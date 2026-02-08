package de.riversroses.kernel.engine;

import de.riversroses.ship.db.ShipRepository;
import de.riversroses.ship.model.Ship;
import de.riversroses.team.db.TeamRepository;
import de.riversroses.team.model.Team;
import de.riversroses.world.db.WorldRepository;
import de.riversroses.world.model.Mission;
import de.riversroses.world.model.SpawnedResource;
import de.riversroses.world.model.Station;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.runtime.event.ApplicationShutdownEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import io.micronaut.scheduling.annotation.Scheduled;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Singleton
@Slf4j
@AllArgsConstructor
@Introspected
public class GameStatePersistenceService implements ApplicationEventListener<ServerStartupEvent> {
  private final ShipRepository shipRepo;
  private final TeamRepository teamRepo;
  private final WorldRepository worldRepo;
  private final ObjectMapper objectMapper;
  private final GameProperties props;
  private static boolean isLoaded = false;

  @Override
  public void onApplicationEvent(ServerStartupEvent event) {
    loadState();
  }

  @Scheduled(fixedRate = "1m")
  public void autoSave() {
    saveState();
  }

  @EventListener
  public void onShutdown(ApplicationShutdownEvent event) {
    saveState();
  }

  private synchronized void saveState() {
    if (!isLoaded) {
      log.warn("Attempted to save game state before loading completed. Skipping save.");
      return;
    }
    try {
      if (props.getSavePath() == null || props.getSavePath().isBlank()) {
        log.warn("No savePath configured, skipping save.");
        return;
      }
      File file = new File(props.getSavePath());
      if (file.getParentFile() != null && !file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }
      GameStateSnapshot snapshot = new GameStateSnapshot();
      snapshot.setShips(List.copyOf(shipRepo.getAllShips()));
      snapshot.setTeams(List.copyOf(teamRepo.all().values()));
      snapshot.setResources(List.copyOf(worldRepo.getResources()));
      snapshot.setMissions(List.copyOf(worldRepo.getMissions()));

      snapshot.setStations(List.copyOf(worldRepo.getStations()));

      try (FileOutputStream fos = new FileOutputStream(file)) {
        objectMapper.writeValue(fos, snapshot);
      }
      log.info("Game state saved to {}", props.getSavePath());
    } catch (IOException e) {
      log.error("Failed to save game state", e);
    }
  }

  private synchronized void loadState() {
    try {
      if (props.getSavePath() == null || props.getSavePath().isBlank()) {
        log.warn("No savePath configured, skipping load.");
        isLoaded = true;
        return;
      }
      File file = new File(props.getSavePath());
      if (!file.exists()) {
        log.info("No saved game state found. Starting fresh.");
        isLoaded = true;
        return;
      }
      try (FileInputStream fis = new FileInputStream(file)) {
        GameStateSnapshot snapshot = objectMapper.readValue(fis, GameStateSnapshot.class);
        Instant now = Instant.now();
        if (snapshot.getTeams() != null) {
          snapshot.getTeams().forEach(t -> teamRepo.restore(t));
        }
        if (snapshot.getShips() != null) {
          snapshot.getShips().forEach(ship -> {
            ship.setLastSimulatedAt(now);
            ship.setLastChangedAt(now);
            ship.setLastCommandAt(now);
            if (ship.getToken() != null) {
              shipRepo.save(ship);
            }
          });
        }
        if (snapshot.getResources() != null) {
          snapshot.getResources().forEach(worldRepo::addResource);
        }
        if (snapshot.getMissions() != null) {
          snapshot.getMissions().forEach(worldRepo::addMission);
        }

        if (snapshot.getStations() != null) {
          snapshot.getStations().forEach(worldRepo::registerStation);
          worldRepo.rebuildIndices();
        }

        log.info("Game state loaded successfully: {} ships, {} stations",
            shipRepo.getAllShips().size(), worldRepo.getStations().size());
        isLoaded = true;
      }
    } catch (IOException e) {
      log.error("Failed to load game state", e);
      isLoaded = true;
    }
  }

  @Data
  @NoArgsConstructor
  @Serdeable
  static class GameStateSnapshot {
    private List<Ship> ships;
    private List<Team> teams;
    private List<SpawnedResource> resources;
    private List<Mission> missions;
    private List<Station> stations;
  }
}
