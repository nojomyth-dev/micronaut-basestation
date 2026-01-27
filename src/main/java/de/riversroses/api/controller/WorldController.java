package de.riversroses.api.controller;

import de.riversroses.api.dto.missions.MissionDto;
import de.riversroses.api.dto.world.DepotDto;
import de.riversroses.api.dto.world.ResourceDto;
import de.riversroses.api.dto.world.WorldSnapshotResponse;
import de.riversroses.application.service.WorldEngine;
import de.riversroses.config.GameProperties;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import java.util.List;

@Controller("/world")
public class WorldController {

  private final GameProperties props;
  private final WorldEngine worldEngine;

  public WorldController(GameProperties props, WorldEngine worldEngine) {
    this.props = props;
    this.worldEngine = worldEngine;
  }

  @Get("/snapshot")
  public WorldSnapshotResponse snapshot() {
    List<DepotDto> depotDtos = props.getDepots().stream()
        .map(d -> new DepotDto(d.getId(), d.getX(), d.getY(), d.getName()))
        .toList();

    List<MissionDto> missionDtos = worldEngine.getActiveMissions().stream()
        .map(m -> new MissionDto(
            m.getId(),
            m.getDescription(),
            m.getTarget().getX(),
            m.getTarget().getY(),
            m.getReward(),
            m.getExpiresAt().getEpochSecond()))
        .toList();

    List<ResourceDto> resourceDtos = worldEngine.getAllResources().stream()
        .map(r -> new ResourceDto(
            r.getId(),
            r.getType().name(),
            r.getValue(),
            r.getPosition().getX(),
            r.getPosition().getY()))
        .toList();

    return new WorldSnapshotResponse(
        props.getWorld().getWidth(),
        props.getWorld().getHeight(),
        props.getHomeBase().getX(),
        props.getHomeBase().getY(),
        props.getHomeBase().getRefillRadius(),
        worldEngine.getAllShips().stream()
            .map(s -> new de.riversroses.api.dto.ships.ShipMarkerDto(
                s.getShipId(),
                s.getTeamName(),
                s.getPosition().getX(),
                s.getPosition().getY(),
                s.getHeadingDeg(),
                s.getSpeed(),
                s.getFuel()))
            .toList(),
        depotDtos,
        missionDtos,
        resourceDtos);
  }

}
