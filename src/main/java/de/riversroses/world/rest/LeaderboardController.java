package de.riversroses.world.rest;

import de.riversroses.team.db.TeamRepository;
import de.riversroses.team.model.Team;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Comparator;
import java.util.List;

@Controller("/leaderboard")
@AllArgsConstructor
@Data
public class LeaderboardController {

  private final TeamRepository teamRepo;

  @Get
  public List<LeaderboardEntry> getLeaderboard() {
    return teamRepo.all().values().stream()
        .sorted(Comparator.comparingLong(Team::getCredits).reversed())
        .limit(20)
        .map(t -> new LeaderboardEntry(t.getDisplayName(), t.getCredits()))
        .toList();
  }

  @Serdeable
  public record LeaderboardEntry(String team, long score) {}
}
