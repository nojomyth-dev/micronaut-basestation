package de.riversroses.world.model;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record SpawnedResource(
        String id,
        Vector2 position,
        int value,
        String oreId,
        OreBehavior behavior) {
}