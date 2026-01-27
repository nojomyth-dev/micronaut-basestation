package de.riversroses.application.simulation;

import de.riversroses.application.service.WorldEngine;
import de.riversroses.config.GameProperties;
import de.riversroses.domain.model.SpawnedResource;
import de.riversroses.domain.model.Vector2;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Singleton
@Slf4j
@AllArgsConstructor
@Data
public class ResourceSpawner {

  private final WorldEngine engine;
  private final GameProperties props;

  public void tick() {
    if (engine.getResourceCount() < props.getWorld().getNodesOnMap()) {
      spawnRandomNode();
    }
  }

  private void spawnRandomNode() {
    double w = props.getWorld().getWidth();
    double h = props.getWorld().getHeight();

    double x = ThreadLocalRandom.current().nextDouble(0, w);
    double y = ThreadLocalRandom.current().nextDouble(0, h);

    var oreConfigs = props.getWorld().getOres();

    if (oreConfigs.isEmpty()) {
      log.warn("No ore configuration found! Cannot spawn resources.");
      return;
    }

    int totalWeight = oreConfigs.values().stream()
        .mapToInt(GameProperties.Ore::getWeight)
        .sum();

    int roll = ThreadLocalRandom.current().nextInt(totalWeight);
    String selectedTypeStr = null;
    GameProperties.Ore selectedConfig = null;

    for (Map.Entry<String, GameProperties.Ore> entry : oreConfigs.entrySet()) {
      roll -= entry.getValue().getWeight();
      if (roll < 0) {
        selectedTypeStr = entry.getKey();
        selectedConfig = entry.getValue();
        break;
      }
    }

    if (selectedTypeStr == null)
      return;

    try {
      SpawnedResource.ResourceType type = SpawnedResource.ResourceType.valueOf(selectedTypeStr.toUpperCase());

      SpawnedResource node = SpawnedResource.builder()
          .id(UUID.randomUUID().toString())
          .position(new Vector2(x, y))
          .type(type)
          .value(selectedConfig.getValue())
          .build();

      engine.addResource(node);

    } catch (IllegalArgumentException e) {
      log.error("Configured type '{}' is invalid", selectedTypeStr.toUpperCase());
    }
  }
}
