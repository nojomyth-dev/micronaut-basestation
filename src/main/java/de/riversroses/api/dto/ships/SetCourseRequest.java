package de.riversroses.api.dto.ships;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Serdeable
public record SetCourseRequest(
    @NotNull @Min(0) @Max(359) Integer headingDeg,
    @NotNull @Min(0) @Max(1000) Integer speed
) {}
