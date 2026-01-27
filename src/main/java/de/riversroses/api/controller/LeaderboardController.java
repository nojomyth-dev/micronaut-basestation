package de.riversroses.api.controller;

import de.riversroses.application.service.WorldEngine;
import de.riversroses.domain.model.Ship;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.serde.annotation.Serdeable;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;

@Controller("/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

  private final WorldEngine engine;

  @Get
  public List<LeaderboardEntry> getLeaderboard() {
    return engine.getAllShips().stream()
        .sorted(Comparator.comparingLong(Ship::getCredits).reversed())
        .limit(20)
        .map(s -> new LeaderboardEntry(s.getTeamName(), s.getCredits()))
        .toList();
  }

  @Serdeable
  public record LeaderboardEntry(String team, long score) {
  }
}
