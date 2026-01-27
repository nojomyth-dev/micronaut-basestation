package de.riversroses.application.simulation;

import de.riversroses.application.service.WorldEngine;
import de.riversroses.config.GameProperties;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Data
@Singleton
@Slf4j
@AllArgsConstructor
public class GameTickScheduler {

  private final GameProperties props;
  private final WorldEngine engine;
  private final PhysicsEngine physics;
  private final ResourceSpawner resourceSpawner;
  private final MissionSpawner missionSpawner;

  @Scheduled(fixedDelay = "${game.tick.periodMs:500}ms")
  void tick() {

    if (!props.getTick().isEnabled())
      return;
    
    processQueue();
    resourceSpawner.tick();
    missionSpawner.tick();

    Instant now = Instant.now();
    double pickupRange = props.getPhysics().getCollectionRadius();

    engine.getAllShips().forEach(ship -> {
      physics.tickShip(ship, now);

      engine.processCollisions(ship, pickupRange);
      engine.checkMissionCompletions(ship);

      if (ship.getFuel() <= 0 && ship.getSpeed() > 0) {
        physics.respawn(ship);
      }
    });
  }

  private void processQueue() {
    engine.processPendingCommands();
  }
}
