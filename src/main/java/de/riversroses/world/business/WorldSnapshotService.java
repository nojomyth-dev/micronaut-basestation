package de.riversroses.world.business;

import java.util.List;
import de.riversroses.kernel.engine.GameProperties;
import de.riversroses.ship.db.ShipRepository;
import de.riversroses.ship.dto.ShipMarkerDto;
import de.riversroses.world.db.WorldRepository;
import de.riversroses.world.dto.MissionDto;
import de.riversroses.world.dto.ResourceDto;
import de.riversroses.world.dto.StationDto;
import de.riversroses.world.dto.WorldSnapshotResponse;
import io.micronaut.core.annotation.Introspected;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;

@Singleton
@AllArgsConstructor
@Introspected
@Data
public class WorldSnapshotService {
  private final GameProperties props;
  private final WorldRepository worldRepo;
  private final ShipRepository shipRepo;

  public WorldSnapshotResponse snapshot() {
    List<StationDto> stationDtos = worldRepo.getStations().stream()
        .map(s -> new StationDto(
            s.id(),
            s.position().x(),
            s.position().y(),
            s.name()))
        .toList();
    List<MissionDto> missionDtos = worldRepo.getMissions().stream()
        .map(m -> new MissionDto(
            m.id(),
            m.description(),
            m.target().x(),
            m.target().y(),
            m.reward()))
        .toList();
    List<ResourceDto> resourceDtos = List.of();
    List<ShipMarkerDto> shipDtos = shipRepo.getAllShips().stream()
        .map(s -> new ShipMarkerDto(
            s.getShipId(),
            s.getDisplayName(),
            s.getTeamName(),
            s.getPosition().x(),
            s.getPosition().y(),
            s.getHeadingDeg(),
            s.getSpeed()))
        .toList();
    return new WorldSnapshotResponse(
        props.getWorld().getWidth(),
        props.getWorld().getHeight(),
        props.getHomeBase().getX(),
        props.getHomeBase().getY(),
        props.getHomeBase().getRefillRadius(),
        shipDtos,
        stationDtos,
        missionDtos,
        resourceDtos);
  }
}
