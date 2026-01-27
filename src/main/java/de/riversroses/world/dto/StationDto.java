package de.riversroses.world.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@Introspected
public record StationDto(
    String id,
    double x,
    double y,
    String name,
    boolean allowsRefill,
    boolean allowsUnload
) {}