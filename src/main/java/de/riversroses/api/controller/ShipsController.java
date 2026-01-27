package de.riversroses.api.controller;

import de.riversroses.api.dto.ships.*;
import de.riversroses.application.service.WorldEngine;
import de.riversroses.config.GameProperties;
import de.riversroses.domain.model.Ship;
import de.riversroses.domain.model.Vector2;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.HttpStatus;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Controller("/ships")
@AllArgsConstructor
@Slf4j
public class ShipsController {

  private final WorldEngine engine;
  private final GameProperties props;

  @Post("/register")
  public RegisterShipResponse register(@Body @Valid RegisterShipRequest req) {
    String cleanTeam = req.getTeamName().trim().substring(0, Math.min(req.getTeamName().length(), 30));

    Ship ship = engine.register(req.getToken(), cleanTeam);

    return RegisterShipResponse.builder()
        .shipId(ship.getShipId())
        .startX(ship.getPosition().getX())
        .startY(ship.getPosition().getY())
        .fuelMax(props.getPhysics().getMaxFuel())
        .build();
  }

  @Post("/me/course")
  public HttpResponse<?> setCourse(@Header("X-Token") String token, @Body @Valid SetCourseRequest req) {
    engine.queueCommand(token, ship -> {
      ship.setHeadingDeg(req.getHeadingDeg());

      int safeSpeed = Math.min(props.getPhysics().getMaxSpeed(), Math.max(0, req.getSpeed()));
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

    // We handle this synchronously here to give immediate feedback on earnings
    // Locking the ship ensures we don't race with the game loop trying to update
    // cargo
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
    return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
  }

  private ShipStatusResponse toDto(Ship ship) {
    return ShipStatusResponse.builder()
        .shipId(ship.getShipId())
        .teamName(ship.getTeamName())
        .x(ship.getPosition().getX())
        .y(ship.getPosition().getY())
        .headingDeg(ship.getHeadingDeg())
        .speed(ship.getSpeed())
        .fuel(ship.getFuel())
        .credits(ship.getCredits())
        .cargo(ship.getCargo())
        .build();
  }

  @Serdeable
  public record ShipConfigDto(Boolean autoCollect) {
  }

  @Serdeable
  public record UnloadResponse(int itemsSold, long earned, long totalCredits) {
  }
}