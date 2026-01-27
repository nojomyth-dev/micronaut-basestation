package de.riversroses.api.controller;

import de.riversroses.api.dto.radar.RadarScanResponse;
import de.riversroses.application.service.WorldEngine;
import de.riversroses.config.GameProperties;
import de.riversroses.domain.model.Ship;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Optional;

@Controller("/scan")
@RequiredArgsConstructor
@Slf4j
public class RadarController {

  private final WorldEngine engine;
  private final GameProperties props;

  @Get
  public RadarScanResponse scan(
      @Header("X-Token") String token,
      @QueryValue Optional<Double> radius) {
    Ship ship = engine.getShipByToken(token)
        .orElseThrow(() -> new HttpStatusException(HttpStatus.UNAUTHORIZED, "Unknown Token"));

    // Calculate requested radius
    double maxR = props.getScan().getMaxRadius();
    double reqR = radius.orElse(200.0);
    double actualRadius = Math.max(10.0, Math.min(maxR, reqR));

    // Calculate Cost
    double cost = props.getScan().getBaseCost() + (actualRadius * props.getScan().getCostPerRadiusUnit());

    // Deduct Fuel
    synchronized (ship) {
      if (ship.getFuel() < cost) {
        // Not enough fuel?
        // Return empty result to punish them for not checking fuel :>
        return RadarScanResponse.builder()
            .ships(Collections.emptyList())
            .resources(Collections.emptyList())
            .build();
      }
      ship.setFuel(ship.getFuel() - cost);
    }

    // Perform Scan
    var result = engine.scan(ship, actualRadius);

    return RadarScanResponse.builder()
        .ships(result.ships().stream()
            .map(s -> RadarScanResponse.FoundShip.builder()
                .shipId(s.getShipId())
                .teamName(s.getTeamName())
                .x(s.getPosition().getX())
                .y(s.getPosition().getY())
                .speed(s.getSpeed())
                .heading(s.getHeadingDeg())
                .build())
            .toList())
        .resources(result.resources().stream()
            .map(r -> RadarScanResponse.FoundResource.builder()
                .id(r.getId())
                .type(r.getType().name())
                .value(r.getValue())
                .x(r.getPosition().getX())
                .y(r.getPosition().getY())
                .build())
            .toList())
        .build();
  }
}