package de.riversroses.world.model;

public record Station(
    String id,
    String name,
    Vector2 position,
    double interactionRadius,
    boolean allowsRefill,
    boolean allowsUnload,
    double fuelPriceMultiplier,
    double cargoPriceMultiplier) {
}
