package de.riversroses.world.business;

import java.time.Instant;
import java.util.Iterator;

import de.riversroses.kernel.engine.GameProperties;
import de.riversroses.ship.model.Ship;
import de.riversroses.team.db.TeamRepository;
import de.riversroses.world.db.WorldRepository;
import de.riversroses.world.model.Mission;
import de.riversroses.world.model.SpawnedResource;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
@AllArgsConstructor
@Data
public class InteractionService {

  private final GameProperties props;
  private final WorldRepository worldRepo;
  private final TeamRepository teamRepo;

  public void processInteractions(Ship ship, Instant now) {
    double pickupRadius = props.getPhysics().getCollectionRadius();

    // Resources
    Iterator<SpawnedResource> resIt = worldRepo.getResources().iterator();
    while (resIt.hasNext()) {
      SpawnedResource res = resIt.next();
      if (ship.getPosition().distanceTo(res.position()) <= pickupRadius) {
        applyResourceEffect(ship, res);
        worldRepo.removeResource(res.id());
      }
    }

    // Missions
    double missionRadius = 20.0; // TODO config
    Iterator<Mission> missionIt = worldRepo.getMissions().iterator();
    while (missionIt.hasNext()) {
      Mission m = missionIt.next();
      if (ship.getPosition().distanceTo(m.target()) <= missionRadius) {
        completeMission(ship, m);
        worldRepo.removeMission(m.id());
      }
    }
  }

  private void applyResourceEffect(Ship ship, SpawnedResource res) {
    switch (res.behavior()) {
      case CARGO -> {
        if (ship.isAutoCollect()) {
          ship.getCargo().merge(res.oreId(), 1, Integer::sum);
        }
      }
      case INSTANT_CREDITS -> teamRepo.addCredits(ship.getTeamId(), res.value());
      case INSTANT_FUEL -> {
        double newFuel = Math.min(props.getPhysics().getMaxFuel(), ship.getFuel() + res.value());
        ship.setFuel(newFuel);
      }
    }
  }

  private void completeMission(Ship ship, Mission m) {
    teamRepo.addCredits(ship.getTeamId(), m.reward());
    log.info("Team {} completed mission {} (+{} credits) via ship {}",
        ship.getTeamId(), m.description(), m.reward(), ship.getShipId());
  }
}
