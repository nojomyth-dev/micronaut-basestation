package de.riversroses.world.business;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import de.riversroses.ship.db.ShipRepository;
import de.riversroses.ship.dto.ShipMarkerDto;
import de.riversroses.ship.model.Ship;
import de.riversroses.world.db.WorldRepository;
import de.riversroses.world.ws.DeltaOp;
import de.riversroses.world.ws.MissionRemove;
import de.riversroses.world.ws.ResourceRemove;
import de.riversroses.world.ws.ShipUpsert;
import de.riversroses.world.ws.WorldDelta;
import io.micronaut.core.annotation.Introspected;
import jakarta.inject.Singleton;
import lombok.Data;

@Singleton
@Introspected
@Data
public class WorldDeltaService {

  private final ShipRepository shipRepo;
  private final WorldRepository worldRepo;

  public WorldDeltaService(ShipRepository shipRepo, WorldRepository worldrepo) {
    this.shipRepo = shipRepo;
    this.worldRepo = worldrepo;
  }

  private Instant lastShipBroadcastCutoff = Instant.EPOCH;

  public WorldDelta computeDelta(long tick) {

    List<DeltaOp> ops = new ArrayList<>();
    Instant newCutoff = Instant.now();

    for (Ship s : shipRepo.getAllShips()) {

      Instant changed = s.getLastChangedAt();

      if (changed != null && changed.isAfter(lastShipBroadcastCutoff)) {
        ops.add(new ShipUpsert(new ShipMarkerDto(
            s.getShipId(),
            s.getTeamName(),
            s.getPosition().x(),
            s.getPosition().y(),
            s.getHeadingDeg(),
            s.getSpeed(),
            s.getFuel())));
      }
    }

    lastShipBroadcastCutoff = newCutoff;

    worldRepo.drainChangedResourceIds();

    for (String id : worldRepo.drainRemovedResourceIds()) {
      ops.add(new ResourceRemove(id));
    }

    for (String id : worldRepo.drainRemovedMissionIds()) {
      ops.add(new MissionRemove(id));
    }

    return new WorldDelta(tick, ops);
  }
}
