package de.riversroses.api.dto.world;

import de.riversroses.api.dto.missions.MissionDto;
import de.riversroses.api.dto.ships.ShipMarkerDto;
import de.riversroses.config.GameProperties;
import de.riversroses.domain.model.Ship;
import io.micronaut.serde.annotation.Serdeable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Serdeable
public record WorldSnapshotResponse(
    Double width,
    Double height,
    Double homeX,
    Double homeY,
    Double refillRadius,
    List<ShipMarkerDto> ships,
    List<DepotDto> depots,
    List<MissionDto> missions,
    List<ResourceDto> resources
) {

  public static WorldSnapshotResponse from(GameProperties props, Collection<Ship> ships) {
    List<ShipMarkerDto> shipDtos = ships.stream()
        .map(s -> new ShipMarkerDto(
            s.getShipId(),
            s.getTeamName(),
            s.getPosition().getX(),
            s.getPosition().getY(),
            s.getHeadingDeg(),
            s.getSpeed(),
            s.getFuel()
        ))
        .toList();

    return new WorldSnapshotResponse(
        props.getWorld().getWidth(),
        props.getWorld().getHeight(),
        props.getHomeBase().getX(),
        props.getHomeBase().getY(),
        props.getHomeBase().getRefillRadius(),
        shipDtos,
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList()
    );
  }
}
