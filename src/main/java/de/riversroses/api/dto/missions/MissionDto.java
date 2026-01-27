package de.riversroses.api.dto.missions;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Serdeable
@Introspected
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionDto {

  private String id;
  private String description;
  private double x;
  private double y;
  private int reward;
  private long expiresAtEpoch;
}
