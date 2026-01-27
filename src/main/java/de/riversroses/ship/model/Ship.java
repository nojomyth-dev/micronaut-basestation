package de.riversroses.ship.model;

import de.riversroses.world.model.Vector2;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Ship {
  private String token;
  private String shipId;

  private volatile String teamId; // canonical team id (normalized)
  private volatile String teamName; // display name

  // Position
  private volatile Vector2 position;

  // Navigation
  private volatile Double targetX;
  private volatile Double targetY;

  // Display / Physics
  private volatile double headingDeg;
  private volatile double speed;
  private volatile double fuel;

  // Inventory (oreId -> count)
  private final Map<String, Integer> cargo = new ConcurrentHashMap<>();
  private volatile boolean autoCollect = true;

  // Meta
  private volatile Instant lastSimulatedAt;
  private volatile Instant lastChangedAt;
  private volatile Instant lastCommandAt;

  public boolean isAt(Vector2 otherPos, double radius) {
    return position.distanceTo(otherPos) <= radius;
  }
}
