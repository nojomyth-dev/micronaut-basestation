package de.riversroses.team.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Serdeable
@Introspected
public record RegisterTeamRequest(
        @NotBlank String token,
        @NotBlank String teamName,
        @NotBlank String planetName) {
}
