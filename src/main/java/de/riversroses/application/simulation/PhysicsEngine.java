package de.riversroses.application.simulation;

import de.riversroses.config.GameProperties;
import de.riversroses.domain.model.Ship;
import de.riversroses.domain.model.Vector2;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;

@Data
@AllArgsConstructor
@Singleton
@Slf4j
public class PhysicsEngine {

  private final GameProperties props;

  public void tickShip(Ship ship, Instant now) {
    Instant last = ship.getLastSimulatedAt();
    if (last == null) {
      ship.setLastSimulatedAt(now);
      return;
    }

    long millis = Duration.between(last, now).toMillis();
    if (millis <= 0)
      return;

    double dt = millis / 1000.0;
    ship.setLastSimulatedAt(now);

    // Safety check for null targets (legacy ships or bad inits)
    if (ship.getTargetX() == null || ship.getTargetY() == null) {
      ship.setTargetX(ship.getPosition().getX());
      ship.setTargetY(ship.getPosition().getY());
    }

    // Calculate Vector to Target
    double dx = ship.getTargetX() - ship.getPosition().getX();
    double dy = ship.getTargetY() - ship.getPosition().getY();
    double distToTarget = Math.sqrt(dx * dx + dy * dy);

    // Check if arrived
    if (distToTarget < 1.0) {
      ship.setSpeed(0);
      return;
    }

    // Update Heading for display (Atan2 returns radians)
    // We swap args (dx, dy) because our 0-degree is North (Y+), not East (X+)
    double angleRad = Math.atan2(dx, -dy);
    ship.setHeadingDeg(Math.toDegrees(angleRad));

    // Calculate Movement
    // If we are closer than one tick of movement, just snap to target
    double stepDistance = ship.getSpeed() * dt;

    double moveX, moveY;

    if (stepDistance >= distToTarget) {
      // Arrive instantly
      moveX = dx;
      moveY = dy;
      ship.setSpeed(0); // Arrived thus stop
    } else {
      // Move partially
      // Normalize vector (dx/dist, dy/dist) and multiply by step
      moveX = (dx / distToTarget) * stepDistance;
      moveY = (dy / distToTarget) * stepDistance;
    }

    Vector2 oldPos = ship.getPosition();
    double newX = oldPos.getX() + moveX;
    double newY = oldPos.getY() + moveY;

    // Clamp to World
    newX = clamp(newX, 0, props.getWorld().getWidth());
    newY = clamp(newY, 0, props.getWorld().getHeight());

    ship.setPosition(new Vector2(newX, newY));

    // Calculate Fuel (Based on actual distance traveled)
    double traveledDist = distance(oldPos.getX(), oldPos.getY(), newX, newY);

    if (traveledDist > 0) {
      int totalItems = ship.getCargo().values().stream().mapToInt(Integer::intValue).sum();
      double baseBurn = props.getPhysics().getFuelPerSecondAtSpeed1();
      double cargoPenalty = props.getPhysics().getFuelPerCargoUnit() * totalItems;

      double totalBurnFactor = baseBurn + cargoPenalty;
      double burned = totalBurnFactor * traveledDist;

      synchronized (ship) {
        ship.setFuel(Math.max(0, ship.getFuel() - burned));
      }
    }
  }

  public void respawn(Ship ship) {
    log.info("Respawning ship: {}", ship.getShipId());

    double x = props.getHomeBase().getX();
    double y = props.getHomeBase().getY();

    ship.setPosition(new Vector2(x, y));
    ship.setTargetX(x);
    ship.setTargetY(y);
    ship.setSpeed(0);
    ship.setHeadingDeg(0);
    ship.setFuel(props.getPhysics().getRespawnFuel());
    ship.getCargo().clear();

    ship.setLastChangedAt(Instant.now());
    ship.setLastSimulatedAt(Instant.now());
  }

  private double clamp(double v, double min, double max) {
    return Math.max(min, Math.min(max, v));
  }

  private double distance(double x1, double y1, double x2, double y2) {
    return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
  }
}
