package de.riversroses.kernel.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Introspected
@ConfigurationProperties("game")
public class GameProperties {
  private Tick tick = new Tick();
  private Physics physics = new Physics();
  private HomeBase homeBase = new HomeBase();
  private World world = new World();
  private Scan scan = new Scan();
  private MissionProviders missionProviders = new MissionProviders();
  private String savePath;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Introspected
  @ConfigurationProperties("tick")
  public static class Tick {
    private boolean enabled = false;
    private Long periodMs = 500L;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Introspected
  @ConfigurationProperties("physics")
  public static class Physics {
    private Double collectionRadius = 15.0D;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Introspected
  @ConfigurationProperties("home-base")
  public static class HomeBase {
    private Double x = 500D;
    private Double y = 500D;
    private Double refillRadius = 40D;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Introspected
  @ConfigurationProperties("world")
  public static class World {
    private Double width = 1000D;
    private Double height = 1000D;
    private Integer nodesOnMap = 20;
    private Integer missions = 5;
    private Map<String, Ore> ores = new HashMap<>();
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Introspected
  @ConfigurationProperties("scan")
  public static class Scan {
    private Double baseCost = 0.0D;
    private Double costPerRadiusUnit = 0.0D;
    private Double maxRadius = 800D;
    private Double missionCompletionRadius = 20.0D;
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Introspected
  public static class Ore {
    private Integer weight;
    private Integer value;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Introspected
  @ConfigurationProperties("mission-providers")
  public static class MissionProviders {
    private List<String> urls = new ArrayList<>();
    private Long pollIntervalMs = 5000L;
    private Long timeoutMs = 2000L;
    private String completionPath = "/missions/complete";
  }
}
