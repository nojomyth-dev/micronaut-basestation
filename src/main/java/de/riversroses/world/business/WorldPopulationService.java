package de.riversroses.world.business;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import de.riversroses.kernel.engine.GameProperties;
import de.riversroses.world.db.WorldRepository;
import de.riversroses.world.model.Mission;
import de.riversroses.world.model.SpawnedResource;
import de.riversroses.world.model.Vector2;
import de.riversroses.world.ore.OreDefinition;
import de.riversroses.world.ore.OreRegistry;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
@AllArgsConstructor
@Data
public class WorldPopulationService {
  private final WorldRepository worldRepo;
  private final GameProperties props;
  private final OreRegistry oreRegistry;
  private final Random rnd = new Random();

  public void tick(Instant now) {
    maintainResources();
  }

  private void maintainResources() {
    if (worldRepo.getResourceCount() < props.getWorld().getNodesOnMap()) {
      spawnRandomNode();
    }
  }

  private void spawnRandomNode() {
    
    double w = props.getWorld().getWidth();
    double h = props.getWorld().getHeight();
    double x = ThreadLocalRandom.current().nextDouble(0, w);
    double y = ThreadLocalRandom.current().nextDouble(0, h);
    OreDefinition def = oreRegistry.weightedRandom(rnd);
    
    if (def == null)
      return;
    
    SpawnedResource node = new SpawnedResource(
        UUID.randomUUID().toString(),
        new Vector2(x, y),
        def.value(),
        def.id(),
        def.behavior());

    worldRepo.addResource(node);
  }
}
