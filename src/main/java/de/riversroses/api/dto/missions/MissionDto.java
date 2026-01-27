package de.riversroses.api.dto.missions;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record MissionDto(
    String id,
    String description,
    double x,
    double y,
    int reward,
    long expiresAtEpoch
) {}
