package de.riversroses.kernel.engine;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import de.riversroses.kernel.event.EventBus;
import de.riversroses.ship.db.ShipRepository;
import de.riversroses.ship.model.Ship;
import de.riversroses.team.db.TeamRepository;
import de.riversroses.world.business.InteractionService;
import de.riversroses.world.business.MissionService;
import de.riversroses.world.business.NavigationService;
import de.riversroses.world.business.WorldDeltaService;
import de.riversroses.world.business.WorldPopulationService;
import de.riversroses.world.business.WorldSnapshotService;
import de.riversroses.world.db.WorldRepository;
import de.riversroses.world.event.WorldDeltaEvent;
import de.riversroses.world.event.WorldSnapshotEvent;
import de.riversroses.world.ore.OreRegistry;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;

@Singleton
@AllArgsConstructor
@Data
public class GameTickScheduler {
  private final GameProperties props;
  private final CommandBus commandBus;
  private final ShipRepository shipRepo;
  private final TeamRepository teamRepo;
  private final WorldRepository worldRepo;
  private final OreRegistry oreRegistry;
  private final NavigationService navigationService;
  private final WorldPopulationService populationService;
  private final InteractionService interactionService;
  private final WorldSnapshotService snapshotService;
  private final WorldDeltaService deltaService;
  private final EventBus eventBus;
  private final MissionService missionService;
  private final AtomicLong tickCounter = new AtomicLong(0);

  @Scheduled(fixedDelay = "${game.tick.periodMs:500}ms")
  public void tick() {
    if (!props.getTick().isEnabled())
      return;
    long tick = tickCounter.incrementAndGet();
    Instant now = Instant.now();
    CommandContext ctx = new CommandContext(
        shipRepo,
        teamRepo,
        worldRepo,
        props,
        oreRegistry);
    commandBus.processAll(ctx);
    populationService.tick(now);
    for (Ship ship : shipRepo.getAllShips()) {
      navigationService.updatePhysics(ship, now);
      interactionService.processInteractions(ship, now);
    }
    missionService.refreshMissionsIfNeeded(now);
    eventBus.publish(new WorldDeltaEvent(deltaService.computeDelta(tick)));
    if (tick % 20 == 0) {
      eventBus.publish(new WorldSnapshotEvent(snapshotService.snapshot()));
    }
  }
}
