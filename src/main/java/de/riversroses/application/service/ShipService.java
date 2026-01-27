package de.riversroses.application.service;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import de.riversroses.config.GameProperties;
import de.riversroses.domain.model.Ship;
import de.riversroses.domain.model.Vector2;
import jakarta.inject.Singleton;

@Singleton
public class ShipService {

  private final GameProperties props;
  private final Map<String, Ship> ships;

  public ShipService(GameProperties props) {
    this.props = props;
    this.ships = new ConcurrentHashMap<>();
  }

  public Ship register(String shipId, String teamName) {
    if (shipId == null || shipId.isBlank()) {
      throw new IllegalArgumentException("shipId must not be blank");
    }
    if (teamName == null || teamName.isBlank()) {
      throw new IllegalArgumentException("teamName must not be blank");
    }

    return ships.computeIfAbsent(shipId, id -> {
      Ship s = new Ship();
      s.setShipId(id);
      s.setTeamName(teamName);

      double x = props.getHomeBase().getX();
      double y = props.getHomeBase().getY();

      s.setPosition(new Vector2(x, y));
      s.setHeadingDeg(0);
      s.setSpeed(0);
      s.setFuel(1000);
      s.setCredits(0);
      s.setLastChangedAt(Instant.now());
      return s;
    });
  }

  public Optional<Ship> find(String shipId) {
    return Optional.ofNullable(ships.get(shipId));
  }

  public Collection<Ship> all() {
    return ships.values();
  }

  public Ship setCourse(String shipId, int headingDeg, int speed) {

    Ship ship = ships.get(shipId);

    if (ship == null) {
      throw new IllegalArgumentException("Unknown shipId: " + shipId);
    }
    
    int clampedSpeed = Math.max(props.getPhysics().getMinSpeed(),
        Math.min(props.getPhysics().getMaxSpeed(), speed));
    ship.setSpeed(clampedSpeed);

    ship.setHeadingDeg(headingDeg);
    ship.setLastChangedAt(Instant.now());

    return ship;
  }

  public Ship refillIfInRange(String shipId) {
    Ship ship = ships.get(shipId);
    if (ship == null) {
      throw new IllegalArgumentException("Unknown shipId: " + shipId);
    }

    Vector2 base = new Vector2(props.getHomeBase().getX(), props.getHomeBase().getY());
    double dx = ship.getPosition().getX() - base.getX();
    double dy = ship.getPosition().getY() - base.getY();
    double dist = Math.sqrt(dx * dx + dy * dy);

    if (dist <= props.getHomeBase().getRefillRadius()) {
      ship.setFuel(1000);
      ship.setLastChangedAt(Instant.now());
    }
    return ship;
  }

  public Ship respawnAtHome(String shipId) {
    Ship ship = ships.get(shipId);
    if (ship == null) {
      throw new IllegalArgumentException("Unknown shipId: " + shipId);
    }

    double x = props.getHomeBase().getX();
    double y = props.getHomeBase().getY();
    ship.setPosition(new Vector2(x, y));
    ship.setSpeed(0);
    ship.setHeadingDeg(0);
    ship.setFuel(props.getPhysics().getRespawnFuel());

    // Lose cargo on death
    ship.getCargo().clear();

    ship.setLastChangedAt(Instant.now());
    return ship;
  }
}
