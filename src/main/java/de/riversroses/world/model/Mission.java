package de.riversroses.world.model;

import java.time.Instant;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Mission(
    String id,
    String description,
    Vector2 target,
    int reward,
    Instant expiresAt) {
}
