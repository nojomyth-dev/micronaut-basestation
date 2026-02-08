package de.riversroses.world.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@Introspected
public record MissionDto(
                String id,
                String description,
                double x,
                double y,
                int reward) {
}
