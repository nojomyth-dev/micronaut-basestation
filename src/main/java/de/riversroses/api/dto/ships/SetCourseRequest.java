package de.riversroses.api.dto.ships;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Serdeable
@Introspected
public class SetCourseRequest {
  @Min(0)
  @Max(359)
  private int headingDeg;

  @Min(0)
  @Max(1000)
  @NotNull
  private Integer speed;
}
