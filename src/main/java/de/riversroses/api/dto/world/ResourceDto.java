package de.riversroses.api.dto.world;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record ResourceDto(
    String id,
    String type,
    Integer value,
    Double x,
    Double y
) {}
