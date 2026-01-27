package de.riversroses.api.dto.ships;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Serdeable
@Introspected
public class ShipMarkerDto {
  String shipId;
  String teamName;
  double x;
  double y;
  double headingDeg;
  double speed;
  double fuel;
}
