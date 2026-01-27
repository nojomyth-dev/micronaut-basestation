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

    // no movement if speed is 0 or no fuel
    if (ship.getSpeed() <= 0 || ship.getFuel() <= 0) {
      return;
    }

    // Capture old position to calculate actual distance later
    Vector2 oldPos = ship.getPosition();

    // Convert heading to radians (0 deg = north)
    double radians = Math.toRadians(ship.getHeadingDeg());

    // sin(0) = 0, sin(90) = 1 -> X is controlled by Sine
    double vx = Math.sin(radians) * ship.getSpeed();

    // cos(0) = 1, cos(90) = 0 -> Y is controlled by Cosine
    double vy = Math.cos(radians) * ship.getSpeed();

    double newX = oldPos.getX() + vx * dt;
    double newY = oldPos.getY() + vy * dt;

    // clamp to world bounds
    newX = clamp(newX, 0, props.getWorld().getWidth());
    newY = clamp(newY, 0, props.getWorld().getHeight());

    // Update position
    ship.setPosition(new Vector2(newX, newY));

    // Calculate actual distance moved (handling wall collisions)
    double dx = newX - oldPos.getX();
    double dy = newY - oldPos.getY();
    double actualDist = Math.sqrt(dx * dx + dy * dy);

    // Fuel calculation
    int totalItems = ship.getCargo().values().stream().mapToInt(Integer::intValue).sum();

    double baseBurn = props.getPhysics().getFuelPerSecondAtSpeed1();
    double cargoPenalty = props.getPhysics().getFuelPerCargoUnit() * totalItems;

    // Formula: (Base + Penalty) * Distance
    double totalBurnFactor = baseBurn + cargoPenalty;
    double burned = totalBurnFactor * actualDist;

    // Synchronized to prevent conflict with Scan deducting fuel at same time
    synchronized (ship) {
      ship.setFuel(Math.max(0, ship.getFuel() - burned));
    }
  }

  public void respawn(Ship ship) {
    log.info("Respawning ship: {}", ship.getShipId());

    double x = props.getHomeBase().getX();
    double y = props.getHomeBase().getY();

    ship.setPosition(new Vector2(x, y));
    ship.setSpeed(0);
    ship.setHeadingDeg(0);
    ship.setFuel(props.getPhysics().getRespawnFuel());

    // Penalty: Lose cargo
    ship.getCargo().clear();

    ship.setLastChangedAt(Instant.now());
    // Reset simulation timer so they don't "jump" if there was a lag
    ship.setLastSimulatedAt(Instant.now());
  }

  private double clamp(double v, double min, double max) {
    return Math.max(min, Math.min(max, v));
  }
}
