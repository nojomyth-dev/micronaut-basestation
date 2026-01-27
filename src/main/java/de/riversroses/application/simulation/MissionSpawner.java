package de.riversroses.application.simulation;

import de.riversroses.application.service.WorldEngine;
import de.riversroses.config.GameProperties;
import de.riversroses.domain.model.Mission;
import de.riversroses.domain.model.Vector2;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Data
@Singleton
@AllArgsConstructor
@Slf4j
public class MissionSpawner {

  private final WorldEngine engine;
  private final GameProperties props;

  public void tick() {
    engine.removeExpiredMissions();

    if (engine.getActiveMissions().size() < props.getWorld().getMissions()) {
      spawnMission();
    }
  }

  private void spawnMission() {
    double w = props.getWorld().getWidth();
    double h = props.getWorld().getHeight();

    double x = ThreadLocalRandom.current().nextDouble(0, w);
    double y = ThreadLocalRandom.current().nextDouble(0, h);

    int reward = ThreadLocalRandom.current().nextInt(100, 500);
    int durationSeconds = ThreadLocalRandom.current().nextInt(60, 300);

    Mission m = Mission.builder()
        .id(UUID.randomUUID().toString())
        .description("Priority Target Alpha")
        .target(new Vector2(x, y))
        .reward(reward)
        .expiresAt(Instant.now().plus(durationSeconds, ChronoUnit.SECONDS))
        .build();

    engine.addMission(m);
  }
}
