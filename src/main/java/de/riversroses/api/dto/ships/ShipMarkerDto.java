package de.riversroses.api.dto.ships;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record ShipMarkerDto(
    String shipId,
    String teamName,
    double x,
    double y,
    double headingDeg,
    double speed,
    double fuel
) {}
