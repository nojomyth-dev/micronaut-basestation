package de.riversroses.api.dto.world;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Serdeable
@Introspected
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepotDto {
  private String id;
  private double x;
  private double y;
  private String name;
}
