package de.riversroses.team.db;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import de.riversroses.team.model.Team;
import io.micronaut.core.annotation.Introspected;
import jakarta.inject.Singleton;
import lombok.Data;
import lombok.NoArgsConstructor;

@Singleton
@NoArgsConstructor
@Data
@Introspected
public class TeamRepository {
  private final Map<String, Team> teamsById = new ConcurrentHashMap<>();
  private final Map<String, Team> teamsByToken = new ConcurrentHashMap<>();

  public Team register(String teamId, String token, String displayName) {
    Team t = new Team(teamId, token, displayName);
    teamsById.put(teamId, t);
    teamsByToken.put(token, t);
    return t;
  }

  public Optional<Team> findByToken(String token) {
    return Optional.ofNullable(teamsByToken.get(token));
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

  public void restore(Team t) {
    teamsById.put(t.getId(), t);
    if (t.getToken() != null) {
      teamsByToken.put(t.getToken(), t);
    }
  }
}
