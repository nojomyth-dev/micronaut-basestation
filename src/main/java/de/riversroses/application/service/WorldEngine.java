package de.riversroses.application.service;

import de.riversroses.application.simulation.GameCommand;
import de.riversroses.config.GameProperties;
import de.riversroses.domain.model.Mission;
import de.riversroses.domain.model.Ship;
import de.riversroses.domain.model.SpawnedResource;
import de.riversroses.domain.model.Vector2;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@Data
@Singleton
@Slf4j
@AllArgsConstructor
public class WorldEngine {

  private final GameProperties props;

  private final Map<String, Ship> shipsByToken = new ConcurrentHashMap<>();
  private final Queue<GameCommand> commandQueue = new ConcurrentLinkedQueue<>();
  private final Map<String, SpawnedResource> resources = new ConcurrentHashMap<>();
  private final Map<String, Mission> missions = new ConcurrentHashMap<>();

  public void addResource(SpawnedResource node) {
    resources.put(node.getId(), node);
  }

  public void removeResource(String id) {
    resources.remove(id);
  }

  public Collection<SpawnedResource> getAllResources() {
    return resources.values();
  }

  public int getResourceCount() {
    return resources.size();
  }

  public void addMission(Mission mission) {
    missions.put(mission.getId(), mission);
  }

  public void removeExpiredMissions() {
    Instant now = Instant.now();
    missions.values().removeIf(m -> m.getExpiresAt().isBefore(now));
  }

  public Collection<Mission> getActiveMissions() {
    return missions.values();
  }

  public Ship register(String token, String teamName) {

    log.info("Registering ship: {} (Token {})", teamName, token);
    return shipsByToken.compute(token, (key, existing) -> {

      Instant now = Instant.now();

      if (existing != null) {

        log.info("Team reconnected: {} (Ship {})", teamName, existing.getShipId());

        existing.setTeamName(teamName);
        existing.setLastCommandAt(now);

        return existing;
      } else {
        Ship s = new Ship();

        s.setToken(token);
        s.setShipId(UUID.randomUUID().toString());
        s.setTeamName(teamName);
        s.setPosition(new Vector2(props.getHomeBase().getX(), props.getHomeBase().getY()));
        s.setHeadingDeg(0);
        s.setTargetX(props.getHomeBase().getX());
        s.setTargetY(props.getHomeBase().getY());
        s.setSpeed(0);
        s.setFuel(props.getPhysics().getMaxFuel());
        s.setLastChangedAt(now);
        s.setLastCommandAt(now);

        log.info("New Ship registered: {} -> {}", teamName, s.getShipId());

        return s;
      }
    });
  }

  public void queueCommand(String token, Consumer<Ship> action) {
    Ship ship = shipsByToken.get(token);
    if (ship != null) {
      commandQueue.offer(new GameCommand(ship.getShipId(), action));
    } else {
      log.warn("Command rejected: Unknown token {}", token);
    }
  }

  public void processPendingCommands() {
    GameCommand cmd;
    int limit = 1000;
    while (limit-- > 0 && (cmd = commandQueue.poll()) != null) {
      try {
        Optional<Ship> s = getShipById(cmd.shipId());
        s.ifPresent(cmd.action());
      } catch (Exception e) {
        log.error("Error processing command for ship {}", cmd.shipId(), e);
      }
    }
  }

  public RadarResult scan(Ship ship, double radius) {
    List<Ship> visibleShips = shipsByToken.values().stream()
        .filter(other -> !other.getShipId().equals(ship.getShipId()))
        .filter(other -> distance(ship.getPosition(), other.getPosition()) <= radius)
        .toList();

    List<SpawnedResource> visibleResources = resources.values().stream()
        .filter(res -> distance(ship.getPosition(), res.getPosition()) <= radius)
        .toList();

    return new RadarResult(visibleShips, visibleResources);
  }

  public void processCollisions(Ship ship, double collectionRadius) {
    Iterator<SpawnedResource> it = resources.values().iterator();

    while (it.hasNext()) {
      SpawnedResource res = it.next();
      if (distance(ship.getPosition(), res.getPosition()) <= collectionRadius) {

        switch (res.getType().getBehavior()) {
          case CARGO -> {
            if (!ship.isAutoCollect()) {
              continue;
            }

            ship.getCargo().merge(res.getType().name(), 1, Integer::sum);
            it.remove();
          }
          case INSTANT_CREDITS -> {
            ship.setCredits(ship.getCredits() + res.getValue());
            it.remove();
          }
          case INSTANT_FUEL -> {
            double newFuel = Math.min(props.getPhysics().getMaxFuel(), ship.getFuel() + res.getValue());
            ship.setFuel(newFuel);
            it.remove();
          }
        }
      }
    }
  }

  public void checkMissionCompletions(Ship ship) {
    double claimRadius = 20.0;
    Iterator<Mission> it = missions.values().iterator();
    while (it.hasNext()) {
      Mission m = it.next();
      if (distance(ship.getPosition(), m.getTarget()) <= claimRadius) {
        ship.setCredits(ship.getCredits() + m.getReward());
        log.info("Ship {} completed mission {} (+{} credits)",
            ship.getShipId(), m.getDescription(), m.getReward());
        it.remove();
      }
    }
  }

  public void dumpCargo(Ship ship) {
    if (ship.getCargo().isEmpty()) {
      return;
    }

    log.info("Ship {} is dumping cargo into space", ship.getShipId());

    ship.getCargo().forEach((typeStr, count) -> {
      try {
        SpawnedResource.ResourceType type = SpawnedResource.ResourceType.valueOf(typeStr);
        GameProperties.Ore conf = props.getWorld().getOres().get(typeStr);
        int val = (conf != null) ? conf.getValue() : 10;

        for (int i = 0; i < count; i++) {
          double offsetX = ThreadLocalRandom.current().nextDouble(-20, 20);
          double offsetY = ThreadLocalRandom.current().nextDouble(-20, 20);

          SpawnedResource res = SpawnedResource.builder()
              .id(UUID.randomUUID().toString())
              .type(type)
              .value(val)
              .position(new Vector2(
                  ship.getPosition().getX() + offsetX,
                  ship.getPosition().getY() + offsetY))
              .build();

          addResource(res);
        }
      } catch (IllegalArgumentException e) {
        log.warn("Cannot respawn unknown cargo type: {}", typeStr);
      }
    });

    ship.getCargo().clear();
  }

  public UnloadResult unload(Ship ship) {
    boolean canUnload = false;
    double range = props.getHomeBase().getRefillRadius();

    Vector2 home = new Vector2(props.getHomeBase().getX(), props.getHomeBase().getY());
    if (distance(ship.getPosition(), home) <= range) {
      canUnload = true;
    }

    if (!canUnload) {
      for (GameProperties.Depot depot : props.getDepots()) {
        Vector2 dPos = new Vector2(depot.getX(), depot.getY());
        if (distance(ship.getPosition(), dPos) <= range) {
          canUnload = true;
          break;
        }
      }
    }

    if (!canUnload) {
      throw new IllegalStateException("Too far from any Base or Depot");
    }

    long totalValue = 0;
    int totalItems = 0;

    for (Map.Entry<String, Integer> entry : ship.getCargo().entrySet()) {
      String typeName = entry.getKey();
      int count = entry.getValue();

      GameProperties.Ore conf = props.getWorld().getOres().get(typeName);

      if (conf != null) {
        totalValue += (long) conf.getValue() * count;
      } else {
        totalValue += count;
      }
      totalItems += count;
    }

    if (totalItems > 0) {
      ship.setCredits(ship.getCredits() + totalValue);
      ship.getCargo().clear();
      log.info("Ship {} sold {} items for {} credits", ship.getShipId(), totalItems, totalValue);
    }

    return new UnloadResult(totalItems, totalValue, ship.getCredits());
  }

  public Collection<Ship> getAllShips() {
    return shipsByToken.values();
  }

  public Optional<Ship> getShipByToken(String token) {
    return Optional.ofNullable(shipsByToken.get(token));
  }

  public Optional<Ship> getShipById(String shipId) {
    return shipsByToken.values().stream()
        .filter(s -> s.getShipId().equals(shipId))
        .findFirst();
  }

  private double distance(Vector2 a, Vector2 b) {
    return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
  }

  public record RadarResult(List<Ship> ships, List<SpawnedResource> resources) {
  }

  public record UnloadResult(int itemsSold, long earned, long totalCredits) {
  }
}
