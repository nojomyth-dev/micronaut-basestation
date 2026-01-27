package de.riversroses.ship.rest;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import de.riversroses.infra.error.DomainException;
import de.riversroses.infra.error.ErrorCode;
import de.riversroses.kernel.engine.CommandBus;
import de.riversroses.kernel.engine.GameProperties;
import de.riversroses.ship.dto.RegisterShipRequest;
import de.riversroses.ship.dto.RegisterShipResponse;
import de.riversroses.ship.dto.SetCourseRequest;
import de.riversroses.ship.dto.ShipConfigDto;
import de.riversroses.ship.dto.ShipStatusResponse;
import de.riversroses.team.db.TeamRepository;
import de.riversroses.team.model.Team;
import de.riversroses.world.business.CommerceService;
import de.riversroses.world.model.Vector2;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Controller("/ships")
@AllArgsConstructor
@Data
@Slf4j
public class ShipsController {

  private final CommandBus commandBus;
  private final TeamRepository teamRepo;
  private final CommerceService commerceService;
  private final GameProperties props;

  @Post("/register")
  public HttpResponse<RegisterShipResponse> register(@Body @Valid RegisterShipRequest req) {

    RegisterShipResponse resp = commandBus.submitAndWait(
        "registerShip",
        ctx -> {
          String teamTrimmed = req.teamName().trim();
          String cleanTeam = teamTrimmed.substring(0, Math.min(teamTrimmed.length(), 30));
          String teamId = cleanTeam.toLowerCase(Locale.ROOT);

          long current = ctx.shipRepo.countByTeamId(teamId);
          boolean tokenAlreadyExists = ctx.shipRepo.findByToken(req.token()).isPresent();

          if (!tokenAlreadyExists && current >= 2) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Team already has the maximum of 2 ships");
          }

          Team team = ctx.teamRepo.getOrCreate(teamId, cleanTeam);

          var ship = ctx.shipRepo.findByToken(req.token()).orElseGet(() -> {
            var s = new de.riversroses.ship.model.Ship();
            s.setToken(req.token());
            s.setShipId(UUID.randomUUID().toString());
            s.setPosition(new Vector2(props.getHomeBase().getX(), props.getHomeBase().getY()));
            s.setTargetX(props.getHomeBase().getX());
            s.setTargetY(props.getHomeBase().getY());
            s.setFuel(props.getPhysics().getMaxFuel());
            s.setLastSimulatedAt(Instant.now());
            s.setLastChangedAt(Instant.now());
            return s;
          });

          ship.setTeamId(team.getId());
          ship.setTeamName(team.getDisplayName());
          ship.setLastCommandAt(Instant.now());
          ctx.shipRepo.save(ship);

          log.info("Registered ship: {} (Team: {})", ship.getShipId(), cleanTeam);

          return new RegisterShipResponse(
              ship.getShipId(),
              ship.getPosition().x(),
              ship.getPosition().y(),
              props.getPhysics().getMaxFuel());
        },
        1500);

    return HttpResponse.ok(resp);
  }

  @Post("/me/course")
  public HttpResponse<?> setCourse(@Header("X-Token") String token, @Body @Valid SetCourseRequest req) {
   
    commandBus.submitVoid("setCourse", ctx -> {
      var ship = ctx.requireShipByToken(token);

      ship.setTargetX(req.targetX());
      ship.setTargetY(req.targetY());

      int safeSpeed = Math.min(props.getPhysics().getMaxSpeed(), Math.max(0, req.speed()));
      ship.setSpeed(safeSpeed);

      ship.setLastCommandAt(Instant.now());
      ship.setLastChangedAt(Instant.now());
      return null;
    });

    return HttpResponse.accepted();
  }

  @Post("/me/refill")
  public HttpResponse<?> refill(@Header("X-Token") String token) {
    commandBus.submitVoid("refill", ctx -> {
      var ship = ctx.requireShipByToken(token);
      commerceService.attemptRefill(ship);
      ship.setLastChangedAt(Instant.now());
      return null;
    });
    return HttpResponse.accepted();
  }

  @Post("/me/config")
  public HttpResponse<?> configure(@Header("X-Token") String token, @Body ShipConfigDto config) {
   
    commandBus.submitVoid("configure", ctx -> {
      var ship = ctx.requireShipByToken(token);
      if (config.autoCollect() != null) {
        ship.setAutoCollect(config.autoCollect());
      }
      ship.setLastChangedAt(Instant.now());
      return null;
    });
   
    return HttpResponse.ok();
  }

  @Post("/me/dump")
  public HttpResponse<?> dump(@Header("X-Token") String token) {
   
    commandBus.submitVoid("dump", ctx -> {
      var ship = ctx.requireShipByToken(token);
      if (ship.getCargo().isEmpty())
        return null;

      log.info("Ship {} dumping cargo", ship.getShipId());

      ship.getCargo().forEach((oreId, count) -> {
        int val = props.getWorld().getOres().getOrDefault(oreId, new GameProperties.Ore(0, 10)).getValue();

        for (int i = 0; i < count; i++) {
          double offsetX = ThreadLocalRandom.current().nextDouble(-20, 20);
          double offsetY = ThreadLocalRandom.current().nextDouble(-20, 20);

          var res = new de.riversroses.world.model.SpawnedResource(
              UUID.randomUUID().toString(),
              ship.getPosition().add(offsetX, offsetY),
              val,
              oreId,
              ctx.oreRegistry.find(oreId)
                  .map(de.riversroses.world.ore.OreDefinition::behavior)
                  .orElse(de.riversroses.world.model.OreBehavior.CARGO));

          ctx.worldRepo.addResource(res);
        }
      });

      ship.getCargo().clear();
      ship.setLastChangedAt(Instant.now());
      return null;
    });
    return HttpResponse.ok();
  }

  @Post("/me/unload")
  public CommerceService.UnloadResult unload(@Header("X-Token") String token) {
    return commandBus.submitAndWait(
        "unload",
        ctx -> {
          var ship = ctx.requireShipByToken(token);
          return commerceService.unloadCargo(ship);
        },
        1500);
  }

  @Get("/me")
  public ShipStatusResponse me(@Header("X-Token") String token) {
    return commandBus.submitAndWait(
        "me",
        ctx -> {
          var ship = ctx.requireShipByToken(token);
          var team = ctx.teamRepo.findById(ship.getTeamId()).orElse(null);
          long teamCredits = team == null ? 0 : team.getCredits();

          return new ShipStatusResponse(
              ship.getShipId(),
              ship.getTeamName(),
              ship.getTeamId(),
              ship.getPosition().x(),
              ship.getPosition().y(),
              ship.getHeadingDeg(),
              ship.getSpeed(),
              ship.getFuel(),
              teamCredits,
              ship.getCargo());
        },
        1500);
  }
}
