package de.riversroses.api.controller;

import de.riversroses.api.dto.ships.RegisterShipRequest;
import de.riversroses.api.dto.ships.RegisterShipResponse;
import de.riversroses.api.dto.ships.SetCourseRequest;
import de.riversroses.api.dto.ships.ShipStatusResponse;
import de.riversroses.application.service.ShipService;
import de.riversroses.domain.model.Ship;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Controller("/ships")
@Slf4j
public class ShipsController {

  private final ShipService shipService;

  public ShipsController(ShipService shipService) {
    this.shipService = shipService;
  }

  @Post("/register")
  public RegisterShipResponse register(@Body @Valid RegisterShipRequest req) {

    log.info("Register ship shipId={} teamName={}", req.getShipId(), req.getTeamName());

    Ship ship = shipService.register(req.getShipId(), req.getTeamName());

    return RegisterShipResponse.builder()
        .shipId(ship.getShipId())
        .startX(ship.getPosition().getX())
        .startY(ship.getPosition().getY())
        .fuelMax(1000)
        .build();
  }

  @Get("/me/{shipId}")
  public ShipStatusResponse me(@PathVariable String shipId) {

    Ship ship = shipService.find(shipId)
        .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Unknown shipId"));

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

  @Post("/me/{shipId}/course")
  public HttpResponse<?> setCourse(@PathVariable String shipId, @Body @Valid SetCourseRequest req) {

    log.info("Set course shipId={} headingDeg={} speed={}", shipId, req.getHeadingDeg(), req.getSpeed());

    shipService.setCourse(shipId, req.getHeadingDeg(), req.getSpeed());

    return HttpResponse.ok();
  }

  @Post("/me/{shipId}/refill")
  public ShipStatusResponse refill(@PathVariable String shipId) {

    Ship ship = shipService.refillIfInRange(shipId);

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
}
