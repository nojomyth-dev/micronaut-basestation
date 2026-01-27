package de.riversroses.application.simulation;

import de.riversroses.config.GameProperties;
import de.riversroses.domain.model.Ship;
import de.riversroses.domain.model.Vector2;
import jakarta.inject.Singleton;

import java.time.Duration;
import java.time.Instant;

@Singleton
public class PhysicsEngine {

  private final GameProperties props;

  public PhysicsEngine(GameProperties props) {
    this.props = props;
  }

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

    // Convert heading to radians (0 deg = east)
    double radians = Math.toRadians(ship.getHeadingDeg());
    double vx = Math.cos(radians) * ship.getSpeed();
    double vy = Math.sin(radians) * ship.getSpeed();

    Vector2 pos = ship.getPosition();
    double newX = pos.getX() + vx * dt;
    double newY = pos.getY() + vy * dt;

    // clamp to world bounds
    newX = clamp(newX, 0, props.getWorld().getWidth());
    newY = clamp(newY, 0, props.getWorld().getHeight());
    ship.setPosition(new Vector2(newX, newY));

    // fuel burn
    double burnPerSec = props.getPhysics().getFuelPerSecondAtSpeed1() * ship.getSpeed();
    double burned = burnPerSec * dt;
    ship.setFuel(Math.max(0, ship.getFuel() - burned));
  }

  private double clamp(double v, double min, double max) {
    return Math.max(min, Math.min(max, v));
  }
}
