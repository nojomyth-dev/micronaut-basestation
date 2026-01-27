package de.riversroses.api.dto.world;

import java.util.Collection;
import java.util.List;

import de.riversroses.config.GameProperties;
import de.riversroses.domain.model.Ship;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Serdeable
@Introspected
public class WorldSnapshotResponse {
  
  Double width;
  Double height;
  Double homeX;
  Double homeY;
  Double refillRadius;
  List<ShipMarkerDto> ships;

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
        .build();
  }

  @Value
  @Builder
  @Serdeable
  @Introspected
  public static class ShipMarkerDto {
    String shipId;
    String teamName;
    double x;
    double y;
    double headingDeg;
    double speed;
    double fuel;
  }
}
