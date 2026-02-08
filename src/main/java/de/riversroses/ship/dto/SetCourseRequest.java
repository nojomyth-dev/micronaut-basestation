package de.riversroses.ship.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Serdeable
@Introspected
public record SetCourseRequest(
        @NotBlank String shipId,
        @NotNull Double targetX,
        @NotNull Double targetY) {
}
