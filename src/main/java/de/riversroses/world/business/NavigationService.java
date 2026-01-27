package de.riversroses.world.business;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;

import de.riversroses.kernel.engine.GameProperties;
import de.riversroses.ship.model.Ship;
import de.riversroses.world.db.WorldRepository;
import de.riversroses.world.model.Station;
import de.riversroses.world.model.Vector2;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
@AllArgsConstructor
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

    // If close enough: stop and mark change if they were moving
    if (dist < 1.0) {
      if (ship.getSpeed() != 0) {
        ship.setSpeed(0);
        ship.setLastChangedAt(now);
      }
      return;
    }

    // Heading
    double dx = target.x() - pos.x();
    double dy = target.y() - pos.y();

    double newHeading = Math.toDegrees(Math.atan2(dx, -dy));
    if (Double.compare(ship.getHeadingDeg(), newHeading) != 0) {
      ship.setHeadingDeg(newHeading);
    }

    // Movement
    double step = ship.getSpeed() * dt;
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
      burnFuel(ship, actualDist);
      ship.setLastChangedAt(now);
    }

    if (ship.getFuel() <= 0 && ship.getSpeed() > 0) {
      handleOutOfFuel(ship, now);
    }
  }

  private void burnFuel(Ship ship, double distance) {
    int cargoCount = ship.getCargo().values().stream().mapToInt(Integer::intValue).sum();
    double burnRate = props.getPhysics().getFuelPerSecondAtSpeed1()
        + (props.getPhysics().getFuelPerCargoUnit() * cargoCount);

    double burned = burnRate * distance;
    ship.setFuel(Math.max(0, ship.getFuel() - burned));
  }

  private void handleOutOfFuel(Ship ship, Instant now) {
    log.info("Ship {} ran out of fuel. Respawning.", ship.getShipId());

    Station nearest = worldRepo.getStations().stream()
        .min(Comparator.comparingDouble(s -> s.position().distanceTo(ship.getPosition())))
        .orElseThrow();

    ship.setPosition(nearest.position());
    ship.setTargetX(nearest.position().x());
    ship.setTargetY(nearest.position().y());
    ship.setSpeed(0);
    ship.setHeadingDeg(0);
    ship.setFuel(props.getPhysics().getRespawnFuel());
    ship.getCargo().clear();
    ship.setLastChangedAt(now);
  }
}
