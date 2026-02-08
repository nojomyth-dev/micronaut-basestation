package de.riversroses.ship.model;

import de.riversroses.world.model.Vector2;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Serdeable
public class Ship {
  private String token;
  private String shipId;
  private volatile String teamId;
  private volatile String teamName;
  private volatile String displayName;
  private volatile Vector2 position;
  private volatile Double targetX;
  private volatile Double targetY;
  private volatile double headingDeg;
  private volatile double speed;
  private final Map<String, Integer> cargo = new ConcurrentHashMap<>();
  private volatile Instant lastSimulatedAt;
  private volatile Instant lastChangedAt;
  private volatile Instant lastCommandAt;

  public boolean isAt(Vector2 otherPos, double radius) {
    return position.distanceTo(otherPos) <= radius;
  }
}
