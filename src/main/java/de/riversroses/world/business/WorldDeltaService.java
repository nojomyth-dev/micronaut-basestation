package de.riversroses.world.business;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.riversroses.ship.db.ShipRepository;
import de.riversroses.ship.dto.ShipMarkerDto;
import de.riversroses.ship.model.Ship;
import de.riversroses.world.db.WorldRepository;
import de.riversroses.world.dto.MissionDto;
import de.riversroses.world.dto.ResourceDto;
import de.riversroses.world.ws.DeltaOp;
import de.riversroses.world.ws.MissionRemove;
import de.riversroses.world.ws.MissionUpsert;
import de.riversroses.world.ws.ResourceRemove;
import de.riversroses.world.ws.ResourceUpsert;
import de.riversroses.world.ws.ShipUpsert;
import de.riversroses.world.ws.WorldDelta;
import jakarta.inject.Singleton;
import lombok.Data;

@Singleton
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

    // Resources: upserts + removes
    Set<String> changedRes = worldRepo.drainChangedResourceIds();
    for (String id : changedRes) {
      var r = worldRepo.getResource(id);
      if (r != null) {
        ops.add(new ResourceUpsert(new ResourceDto(
            r.id(),
            r.oreId(),
            r.value(),
            r.position().x(),
            r.position().y())));
      }
    }
    for (String id : worldRepo.drainRemovedResourceIds()) {
      ops.add(new ResourceRemove(id));
    }

    // Missions: upserts + removes
    Set<String> changedM = worldRepo.drainChangedMissionIds();
    for (String id : changedM) {
      var m = worldRepo.getMission(id);
      if (m != null) {
        ops.add(new MissionUpsert(new MissionDto(
            m.id(),
            m.description(),
            m.target().x(),
            m.target().y(),
            m.reward(),
            m.expiresAt().getEpochSecond())));
      }
    }
    for (String id : worldRepo.drainRemovedMissionIds()) {
      ops.add(new MissionRemove(id));
    }

    return new WorldDelta(tick, ops);
  }
}
