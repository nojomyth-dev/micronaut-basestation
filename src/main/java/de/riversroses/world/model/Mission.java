package de.riversroses.world.model;

import java.time.Instant;

public record Mission(
    String id,
    String description,
    Vector2 target,
    int reward,
    Instant expiresAt) {
}
