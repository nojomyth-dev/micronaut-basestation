package de.riversroses.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Introspected
@ConfigurationProperties("game")
public class GameProperties {

  private Tick tick = new Tick();
  private Physics physics = new Physics();
  private HomeBase homeBase = new HomeBase();
  private World world = new World();
  private Scan scan = new Scan();

  private List<Depot> depots = new ArrayList<>();

  @Data
  @Introspected
  public static class Tick {
    private boolean enabled = false;
    private Long periodMs = 500L;
  }

  @Data
  @Introspected
  public static class Physics {
    private Double fuelPerSecondAtSpeed1 = 0.05D;
    private Double fuelPerCargoUnit = 0.02D;

    private Integer minSpeed = 0;
    private Integer maxSpeed = 500;

    private Double maxFuel = 1000D;
    private Double respawnFuel = 800D;

    private Double collectionRadius = 15.0D;
  }

  @Data
  @Introspected
  public static class HomeBase {
    private Double x = 500D;
    private Double y = 500D;
    private Double refillRadius = 40D;
  }

  @Data
  @Introspected
  public static class World {
    private Double width = 1000D;
    private Double height = 1000D;

    private Integer nodesOnMap = 20;

    private Integer missions = 5;

    private Map<String, OreConfig> ores = new HashMap<>();

    // Fallback values
    public World() {
      ores.put("IRON", new OreConfig(70, 10));
      ores.put("GOLD", new OreConfig(20, 50));
      ores.put("DIAMOND", new OreConfig(10, 150));
    }
  }

  @Data
  @Introspected
  public static class Scan {
    private Double baseCost = 5.0D;
    private Double costPerRadiusUnit = 0.05D;
    private Double maxRadius = 800D;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Introspected
  public static class OreConfig {
    private Integer weight;
    private Integer value;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Introspected
  public static class Depot {
    private String id;
    private Double x;
    private Double y;
    private Double costPerFuel;
    private String name;
  }
}
