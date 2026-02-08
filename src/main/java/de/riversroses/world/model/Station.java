package de.riversroses.world.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@Introspected
public record Station(
    String id,
    String name,
    Vector2 position,
    double interactionRadius,
    String ownerToken) {
}
