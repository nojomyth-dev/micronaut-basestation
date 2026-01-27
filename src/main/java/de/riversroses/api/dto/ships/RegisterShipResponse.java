package de.riversroses.api.dto.ships;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record RegisterShipResponse(
    String shipId,
    double startX,
    double startY,
    double fuelMax
) {}
