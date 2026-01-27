package de.riversroses.api.dto.ships;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Introspected
@Serdeable
public class RegisterShipResponse {
  String shipId;
  double startX;
  double startY;
  double fuelMax;
}
