package de.riversroses.ship.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@Introspected
public record ShipMarkerDto(
        String shipId,
        String displayName,
        String teamName,
        double x,
        double y,
        double headingDeg,
        double speed) {
}
