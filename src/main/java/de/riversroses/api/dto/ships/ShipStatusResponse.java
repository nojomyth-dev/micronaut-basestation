package de.riversroses.api.dto.ships;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Value
@Builder
@Serdeable
@Introspected
public class ShipStatusResponse {
  String shipId;
  String teamName;
  double x;
  double y;
  double headingDeg;
  double speed;
  double fuel;
  long credits;
  Map<String, Integer> cargo;
}
