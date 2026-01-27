package de.riversroses.api.dto.ships;

import io.micronaut.serde.annotation.Serdeable;

import java.util.Map;

@Serdeable
public record ShipStatusResponse(
    String shipId,
    String teamName,
    double x,
    double y,
    double headingDeg,
    double speed,
    double fuel,
    long credits,
    Map<String, Integer> cargo
) {}
