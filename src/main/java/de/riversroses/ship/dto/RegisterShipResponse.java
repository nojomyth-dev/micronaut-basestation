package de.riversroses.ship.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@Introspected
public record RegisterShipResponse(
    String shipId,
    double startX,
    double startY,
    double fuelMax) {
}
