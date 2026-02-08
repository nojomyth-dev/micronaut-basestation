package de.riversroses.world.business;

import java.time.Duration;
import java.time.Instant;
import de.riversroses.kernel.engine.GameProperties;
import de.riversroses.ship.model.Ship;
import de.riversroses.world.db.WorldRepository;
import de.riversroses.world.model.Vector2;
import io.micronaut.core.annotation.Introspected;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
@AllArgsConstructor
@Introspected
public class NavigationService {
  private final GameProperties props;
  private final WorldRepository worldRepo;

  public void updatePhysics(Ship ship, Instant now) {
    if (ship.getLastSimulatedAt() == null) {
      ship.setLastSimulatedAt(now);
      ship.setLastChangedAt(now);
      return;
    }
    double dt = Duration.between(ship.getLastSimulatedAt(), now).toMillis() / 1000.0;
    if (dt <= 0)
      return;
    ship.setLastSimulatedAt(now);
    if (ship.getTargetX() == null || ship.getTargetY() == null || ship.getPosition() == null) {
      return;
    }
    Vector2 target = new Vector2(ship.getTargetX(), ship.getTargetY());
    Vector2 pos = ship.getPosition();
    double dist = pos.distanceTo(target);
    if (dist < 1.0) {
      if (ship.getSpeed() != 0) {
        ship.setSpeed(0);
        ship.setLastChangedAt(now);
      }
      return;
    }
    double dx = target.x() - pos.x();
    double dy = target.y() - pos.y();
    double newHeading = Math.toDegrees(Math.atan2(dx, -dy));
    if (Double.compare(ship.getHeadingDeg(), newHeading) != 0) {
      ship.setHeadingDeg(newHeading);
    }
    double constantSpeed = 100.0;
    ship.setSpeed(constantSpeed);
    double step = constantSpeed * dt;
    double actualDist = Math.min(step, dist);
    if (actualDist <= 0) {
      return;
    }
    double moveX = (dx / dist) * actualDist;
    double moveY = (dy / dist) * actualDist;
    Vector2 nextPos = pos.add(moveX, moveY)
        .clamp(props.getWorld().getWidth(), props.getWorld().getHeight());
    boolean moved = nextPos.distanceTo(pos) > 0.0001;
    if (moved) {
      ship.setPosition(nextPos);
      ship.setLastChangedAt(now);
    }
  }
}
