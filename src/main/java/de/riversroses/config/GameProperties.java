package de.riversroses.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;

@Data
@Introspected
@ConfigurationProperties("game")
public class GameProperties {

  private Tick tick = new Tick();
  private Physics physics = new Physics();
  private HomeBase homeBase = new HomeBase();
  private World world = new World();

  @Data
  @Introspected
  public static class Tick {
    private Boolean enabled = false;
    private Long periodMs = 500L;
  }

  @Data
  @Introspected
  public static class Physics {
    /**
     * Fuel burn is: fuelPerSecondAtSpeed1 * speed
     * (simple linear model; we can switch to speed^2 later)
     */
    private Double fuelPerSecondAtSpeed1 = 0.05D;
    private Integer minSpeed = 0;
    private Integer maxSpeed = 500;
    private Double respawnFuel = 800D;
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
  }
}
