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
  private final CommerceService commerceService;

  public void processInteractions(Ship ship, Instant now) {
    double pickupRadius = props.getPhysics().getCollectionRadius();
    Iterator<SpawnedResource> resIt = worldRepo.getResources().iterator();
    while (resIt.hasNext()) {
      SpawnedResource res = resIt.next();
      if (ship.getPosition().distanceTo(res.position()) <= pickupRadius) {
        applyResourceEffect(ship, res);
        worldRepo.removeResource(res.id());
      }
    }
    Iterator<Mission> missionIt = worldRepo.getMissions().iterator();
    while (missionIt.hasNext()) {
      Mission m = missionIt.next();
      if (ship.getPosition().distanceTo(m.target()) <= props.getScan().getMissionCompletionRadius()) {
        worldRepo.markPendingMissionCompletion(m, ship);
      }
    }
    commerceService.autoSellIfNearPlanet(ship);
  }

  private void applyResourceEffect(Ship ship, SpawnedResource res) {
    ship.getCargo().merge(res.oreId(), 1, Integer::sum);
  }
}
