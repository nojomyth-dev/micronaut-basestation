package de.riversroses.ship.rest;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import de.riversroses.infra.error.DomainException;
import de.riversroses.infra.error.ErrorCode;
import de.riversroses.kernel.engine.CommandBus;
import de.riversroses.ship.dto.RegisterShipRequest;
import de.riversroses.ship.dto.RegisterShipResponse;
import de.riversroses.ship.dto.SetCourseRequest;
import de.riversroses.ship.dto.ShipStatusResponse;
import de.riversroses.team.dto.RegisterTeamRequest;
import de.riversroses.team.model.Team;
import de.riversroses.world.business.CommerceService;
import de.riversroses.world.db.WorldRepository;
import de.riversroses.world.model.Station;
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
  private final CommerceService commerceService;
  private final WorldRepository worldRepo;

  /**
   * Step 1: Register Team & Planet
   */
  @Post("/teams/register")
  public HttpResponse<RegisterShipResponse> registerTeam(@Body @Valid RegisterTeamRequest req) {
    RegisterShipResponse resp = commandBus.submitAndWait("registerTeam", ctx -> {
      String teamTrimmed = req.teamName().trim();
      String cleanTeam = teamTrimmed.substring(0, Math.min(teamTrimmed.length(), 30));
      String teamId = cleanTeam.toLowerCase(Locale.ROOT);

      if (ctx.teamRepo.findByToken(req.token()).isPresent()) {
        throw new DomainException(ErrorCode.BAD_REQUEST, "Token is already registered");
      }

      // Create Team
      Team team = ctx.teamRepo.register(teamId, req.token(), cleanTeam);

      // Create Home Planet
      Station planet = commerceService.createHomePlanetForTeam(req.token(), team, req.planetName());

      log.info("Registered team {} with planet {}", cleanTeam, planet.name());

      // Return info (empty ship list initially)
      return new RegisterShipResponse(
          team.getId(),
          planet.id(),
          planet.position().x(),
          planet.position().y(),
          List.of());
    }, 2000);
    return HttpResponse.ok(resp);
  }

  /**
   * Step 2: Construct/Register a Ship
   */
  @Post("/register")
  public HttpResponse<ShipStatusResponse> registerShip(@Header("X-Token") String token,
      @Body @Valid RegisterShipRequest req) {
    ShipStatusResponse resp = commandBus.submitAndWait("registerShip", ctx -> {
      // AUTH: Find Team by Token
      Team team = ctx.teamRepo.findByToken(token)
          .orElseThrow(
              () -> new DomainException(ErrorCode.UNAUTHORIZED, "Token not registered. Please register team first."));

      // LOGIC: Find Spawn Point (Planet)
      Station planet = worldRepo.findPlanetByToken(token)
          .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND, "No home planet found."));

      Vector2 spawnPos = planet.position();

      var ship = new de.riversroses.ship.model.Ship();
      ship.setToken(token);
      ship.setShipId(UUID.randomUUID().toString());
      ship.setPosition(new Vector2(spawnPos.x(), spawnPos.y()));
      ship.setTargetX(spawnPos.x());
      ship.setTargetY(spawnPos.y());
      ship.setHeadingDeg(0);
      ship.setSpeed(0);
      ship.setTeamId(team.getId());
      ship.setTeamName(team.getDisplayName());
      ship.setDisplayName(req.shipName().trim());
      ship.setLastSimulatedAt(Instant.now());
      ship.setLastChangedAt(Instant.now());
      ship.setLastCommandAt(Instant.now());

      ctx.shipRepo.save(ship);
      log.info("Constructed ship {} for team {}", ship.getDisplayName(), team.getDisplayName());

      return new ShipStatusResponse(
          ship.getShipId(),
          ship.getDisplayName(),
          ship.getTeamName(),
          ship.getTeamId(),
          ship.getPosition().x(),
          ship.getPosition().y(),
          ship.getHeadingDeg(),
          ship.getSpeed(),
          team.getCredits(),
          ship.getCargo());
    }, 2000);
    return HttpResponse.created(resp);
  }

  /**
   * Control: Move a specific ship
   */
  @Post("/course")
  public HttpResponse<?> setCourse(@Header("X-Token") String token, @Body @Valid SetCourseRequest req) {
    if (!Double.isFinite(req.targetX()) || !Double.isFinite(req.targetY())) {
      return HttpResponse.badRequest("Invalid coordinates");
    }
    commandBus.submitVoid("setCourse", ctx -> {
      // 1. Validate Token (Auth)
      Team team = ctx.teamRepo.findByToken(token)
          .orElseThrow(() -> new DomainException(ErrorCode.UNAUTHORIZED, "Invalid Token"));

      // 2. Validate Ship Ownership (Control)
      var ship = ctx.shipRepo.findById(req.shipId())
          .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND, "Ship not found"));

      if (!ship.getTeamId().equals(team.getId())) {
        throw new DomainException(ErrorCode.FORBIDDEN, "You do not own this ship");
      }

      ship.setTargetX(req.targetX());
      ship.setTargetY(req.targetY());
      ship.setLastCommandAt(Instant.now());
      ship.setLastChangedAt(Instant.now());
      return null;
    });
    return HttpResponse.accepted();
  }

  @Get("/me")
  public List<ShipStatusResponse> myShips(@Header("X-Token") String token) {
    return commandBus.submitAndWait("myShips", ctx -> {
      Team team = ctx.teamRepo.findByToken(token)
          .orElseThrow(() -> new DomainException(ErrorCode.UNAUTHORIZED, "Unknown Token"));

      return ctx.shipRepo.getAllShips().stream()
          .filter(s -> s.getTeamId().equals(team.getId()))
          .map(ship -> new ShipStatusResponse(
              ship.getShipId(),
              ship.getDisplayName(),
              ship.getTeamName(),
              ship.getTeamId(),
              ship.getPosition().x(),
              ship.getPosition().y(),
              ship.getHeadingDeg(),
              ship.getSpeed(),
              team.getCredits(),
              ship.getCargo()))
          .toList();
    }, 1500);
  }
}
