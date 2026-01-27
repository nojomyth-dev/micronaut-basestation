package de.riversroses.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpawnedResource {
  private String id;
  private Vector2 position;
  private int value;
  private ResourceType type;

  public enum ResourceType {
    // Mining Resources (Go to Cargo)
    IRON(Behavior.CARGO),
    GOLD(Behavior.CARGO),
    DIAMOND(Behavior.CARGO),

    // Crates (Instant Effects)
    CRATECREDITS(Behavior.INSTANT_CREDITS),
    CRATEFUEL(Behavior.INSTANT_FUEL);

    private final Behavior behavior;

    ResourceType(Behavior behavior) {
      this.behavior = behavior;
    }

    public Behavior getBehavior() {
      return behavior;
    }
  }

  public enum Behavior {
    CARGO, // Adds to ship cargo, increases weight
    INSTANT_CREDITS, // Immediately adds credits to score
    INSTANT_FUEL // Immediately refills fuel
  }
}
