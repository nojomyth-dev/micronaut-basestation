package de.riversroses.api.controller;

import de.riversroses.api.dto.world.WorldSnapshotResponse;
import de.riversroses.application.service.ShipService;
import de.riversroses.config.GameProperties;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/world")
public class WorldController {

  private final GameProperties props;
  private final ShipService shipService;

  public WorldController(GameProperties props, ShipService shipService) {
    this.props = props;
    this.shipService = shipService;
  }

  @Get("/snapshot")
  public WorldSnapshotResponse snapshot() {
    return WorldSnapshotResponse.from(props, shipService.all());
  }
}
