package de.riversroses.application.simulation;

import de.riversroses.application.service.ShipService;
import de.riversroses.config.GameProperties;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Singleton
@Slf4j
public class GameTickScheduler {

  private final GameProperties props;
  private final ShipService shipService;
  private final PhysicsEngine physics;

  public GameTickScheduler(GameProperties props, ShipService shipService, PhysicsEngine physics) {
    this.props = props;
    this.shipService = shipService;
    this.physics = physics;
  }

  @Scheduled(fixedDelay = "${game.tick.periodMs:500}ms")
  void tick() {
    if (!props.getTick().getEnabled()) return;
    log.info("here");

    Instant now = Instant.now();
    shipService.all().forEach(ship -> {
      physics.tickShip(ship, now);

      // respawn when out of fuel
      if (ship.getFuel() <= 0 && ship.getSpeed() > 0) {
        shipService.respawnAtHome(ship.getShipId());
      }
    });
  }
}
