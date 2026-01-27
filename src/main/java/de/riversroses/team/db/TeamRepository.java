package de.riversroses.team.db;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import de.riversroses.team.model.Team;
import jakarta.inject.Singleton;
import lombok.Data;
import lombok.NoArgsConstructor;

@Singleton
@NoArgsConstructor
@Data
public class TeamRepository {

  private final Map<String, Team> teamsById = new ConcurrentHashMap<>();

  public Team getOrCreate(String teamId, String displayName) {
    return teamsById.computeIfAbsent(teamId, id -> new Team(id, displayName));
  }

  public Optional<Team> findById(String teamId) {
    return Optional.ofNullable(teamsById.get(teamId));
  }

  public void addCredits(String teamId, long delta) {
    Team t = teamsById.get(teamId);
    if (t != null) {
      t.addCredits(delta);
    }
  }

  public Map<String, Team> all() {
    return teamsById;
  }
}
