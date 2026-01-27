package de.riversroses.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OreNode {
  private String id;
  private Vector2 position;
  private int value; // Credits/Points value
  private OreType type;

  public enum OreType {
    IRON, // Common, Low value
    GOLD, // Rare, High value
    DIAMOND // Very Rare, Very High value
  }
}