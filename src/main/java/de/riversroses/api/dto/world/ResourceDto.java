package de.riversroses.api.dto.world;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDto {
  private String id;
  private String type;
  private Integer value;
  private Double x;
  private Double y;
}
