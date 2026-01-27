package de.riversroses.api.controller;

import de.riversroses.api.dto.missions.MissionDto;
import de.riversroses.application.service.WorldEngine;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Controller("/missions")
@RequiredArgsConstructor
public class MissionController {

  private final WorldEngine engine;

  @Get
  public List<MissionDto> list() {
    return engine.getActiveMissions().stream()
        .map(m -> new MissionDto(
            m.getId(),
            m.getDescription(),
            m.getTarget().getX(),
            m.getTarget().getY(),
            m.getReward(),
            m.getExpiresAt().getEpochSecond()))
        .toList();
  }
}
