package de.riversroses.world.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@Introspected
public record ResourceDto(
        String id,
        String type,
        Integer value,
        Double x,
        Double y) {
}
