package de.riversroses.api.dto.ships;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Serdeable
public record RegisterShipRequest(
    @NotBlank String token,
    @NotBlank String teamName
) {}
