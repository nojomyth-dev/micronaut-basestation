package de.riversroses.api.dto.world;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record DepotDto(
    String id,
    double x,
    double y,
    String name
) {}
