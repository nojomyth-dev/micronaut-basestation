package de.riversroses.world.model;

public record SpawnedResource(
    String id,
    Vector2 position,
    int value,
    String oreId,
    OreBehavior behavior) {
}
