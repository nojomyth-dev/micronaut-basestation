package de.riversroses.ship.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;

@Serdeable
@Introspected
public record ShipStatusResponse(
        String shipId,
        String displayName,
        String teamName,
        String teamId,
        double x,
        double y,
        double headingDeg,
        double speed,
        long teamCredits,
        Map<String, Integer> cargo) {
}
