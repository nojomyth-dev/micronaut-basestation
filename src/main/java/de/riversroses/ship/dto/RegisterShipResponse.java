package de.riversroses.ship.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;

@Serdeable
@Introspected
public record RegisterShipResponse(
        String teamId,
        String planetId,
        double planetX,
        double planetY,
        List<String> shipIds) {
}
