package de.riversroses.api.dto.ships;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Introspected
@Serdeable
public class RegisterShipRequest {
  @NotBlank
  private String shipId;

  @NotBlank
  private String teamName;
}
