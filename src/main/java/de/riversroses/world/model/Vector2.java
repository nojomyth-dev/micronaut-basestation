package de.riversroses.world.model;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Vector2(double x, double y) {
    public double distanceTo(Vector2 other) {
        return Math.hypot(this.x - other.x, this.y - other.y);
    }

    public Vector2 add(double dx, double dy) {
        return new Vector2(this.x + dx, this.y + dy);
    }

    public Vector2 clamp(double width, double height) {
        return new Vector2(
                Math.max(0, Math.min(width, x)),
                Math.max(0, Math.min(height, y)));
    }
}
