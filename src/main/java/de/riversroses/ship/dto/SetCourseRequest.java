package de.riversroses.ship.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Serdeable
@Introspected
public record SetCourseRequest(
    @NotNull Double targetX,
    @NotNull Double targetY,
    @NotNull @Min(0) @Max(1000) Integer speed) {
}
