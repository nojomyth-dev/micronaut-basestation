package de.riversroses.api.controller;

import de.riversroses.api.dto.ships.RegisterShipRequest;
import de.riversroses.api.dto.ships.RegisterShipResponse;
import de.riversroses.api.dto.ships.SetCourseRequest;
import de.riversroses.api.dto.ships.ShipStatusResponse;
import de.riversroses.application.service.WorldEngine;
import de.riversroses.config.GameProperties;
import de.riversroses.domain.model.Ship;
import de.riversroses.domain.model.Vector2;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Controller("/ships")
@AllArgsConstructor
@Slf4j
public class ShipsController {

  private final WorldEngine engine;
  private final GameProperties props;

  @Post("/register")
  public HttpResponse<RegisterShipResponse> register(@Body @Valid RegisterShipRequest req) {
    log.info("{}", req);

    String teamTrimmed = req.teamName().trim();
    String cleanTeam = teamTrimmed.substring(0, Math.min(teamTrimmed.length(), 30));

    Ship ship = engine.register(req.token(), cleanTeam);

    return HttpResponse.ok(new RegisterShipResponse(
        ship.getShipId(),
        ship.getPosition().getX(),
        ship.getPosition().getY(),
        props.getPhysics().getMaxFuel()));
  }

  @Post("/me/course")
  public HttpResponse<?> setCourse(@Header("X-Token") String token, @Body @Valid SetCourseRequest req) {
    engine.queueCommand(token, ship -> {
      // Update Target
      ship.setTargetX(req.targetX());
      ship.setTargetY(req.targetY());

      // Update Speed setting
      int safeSpeed = Math.min(props.getPhysics().getMaxSpeed(), Math.max(0, req.speed()));
      ship.setSpeed(safeSpeed);

      ship.setLastCommandAt(Instant.now());
      ship.setLastChangedAt(Instant.now());
    });

    return HttpResponse.accepted();
  }

  @Post("/me/refill")
  public HttpResponse<?> refill(@Header("X-Token") String token) {
    engine.queueCommand(token, ship -> {
      Vector2 base = new Vector2(props.getHomeBase().getX(), props.getHomeBase().getY());
      double dist = distance(ship.getPosition(), base);

      if (dist <= props.getHomeBase().getRefillRadius()) {
        ship.setFuel(props.getPhysics().getMaxFuel());
        ship.setLastChangedAt(Instant.now());
        log.info("Ship {} refueled", ship.getShipId());
      }
    });
    return HttpResponse.accepted();
  }

  @Post("/me/config")
  public HttpResponse<?> configure(@Header("X-Token") String token, @Body ShipConfigDto config) {
    engine.queueCommand(token, ship -> {
      if (config.autoCollect() != null) {
        ship.setAutoCollect(config.autoCollect());
      }
    });
    return HttpResponse.ok();
  }

  @Post("/me/dump")
  public HttpResponse<?> dump(@Header("X-Token") String token) {
    engine.queueCommand(token, engine::dumpCargo);
    return HttpResponse.ok();
  }

  @Post("/me/unload")
  public UnloadResponse unload(@Header("X-Token") String token) {
    Ship ship = engine.getShipByToken(token)
        .orElseThrow(() -> new HttpStatusException(HttpStatus.UNAUTHORIZED, "Unknown Token"));

    synchronized (ship) {
      try {
        var result = engine.unload(ship);
        return new UnloadResponse(result.itemsSold(), result.earned(), result.totalCredits());
      } catch (IllegalStateException e) {
        throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
      }
    }
  }

  @Get("/me")
  public ShipStatusResponse me(@Header("X-Token") String token) {
    Ship ship = engine.getShipByToken(token)
        .orElseThrow(() -> new HttpStatusException(HttpStatus.UNAUTHORIZED, "Unknown Token"));

    return toDto(ship);
  }

  private double distance(Vector2 a, Vector2 b) {
    double dx = a.getX() - b.getX();
    double dy = a.getY() - b.getY();
    return Math.sqrt(dx * dx + dy * dy);
  }

  private ShipStatusResponse toDto(Ship ship) {
    return new ShipStatusResponse(
        ship.getShipId(),
        ship.getTeamName(),
        ship.getPosition().getX(),
        ship.getPosition().getY(),
        ship.getHeadingDeg(),
        ship.getSpeed(),
        ship.getFuel(),
        ship.getCredits(),
        ship.getCargo());
  }

  @Serdeable
  public record ShipConfigDto(Boolean autoCollect) {
  }

  @Serdeable
  public record UnloadResponse(int itemsSold, long earned, long totalCredits) {
  }
}
