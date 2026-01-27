package de.riversroses.api.dto.world;

import de.riversroses.api.dto.missions.MissionDto;
import de.riversroses.api.dto.ships.ShipMarkerDto;
import de.riversroses.config.GameProperties;
import de.riversroses.domain.model.Ship;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Value
@Builder
@Serdeable
@Introspected
@AllArgsConstructor
public class WorldSnapshotResponse {

  Double width;
  Double height;
  Double homeX;
  Double homeY;
  Double refillRadius;
  List<ShipMarkerDto> ships;
  List<DepotDto> depots;
  List<MissionDto> missions;
  List<ResourceDto> resources;

  public static WorldSnapshotResponse from(GameProperties props, Collection<Ship> ships) {
    return WorldSnapshotResponse.builder()
        .width(props.getWorld().getWidth())
        .height(props.getWorld().getHeight())
        .homeX(props.getHomeBase().getX())
        .homeY(props.getHomeBase().getY())
        .refillRadius(props.getHomeBase().getRefillRadius())
        .ships(ships.stream().map(s -> ShipMarkerDto.builder()
            .shipId(s.getShipId())
            .teamName(s.getTeamName())
            .x(s.getPosition().getX())
            .y(s.getPosition().getY())
            .headingDeg(s.getHeadingDeg())
            .speed(s.getSpeed())
            .fuel(s.getFuel())
            .build()).toList())
        .depots(Collections.emptyList())
        .missions(Collections.emptyList())
        .resources(Collections.emptyList())
        .build();
  }
}
