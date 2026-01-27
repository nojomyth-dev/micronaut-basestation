package de.riversroses.world.ore;

import de.riversroses.kernel.engine.GameProperties;
import de.riversroses.world.model.OreBehavior;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@AllArgsConstructor
@Data
public class OreRegistry {

  private final GameProperties props;
  private final Map<String, OreDefinition> defs = new ConcurrentHashMap<>();

  @PostConstruct
  public void init() {
    // Register ore definitions from config at startup
    for (var entry : props.getWorld().getOres().entrySet()) {
      String oreId = canonical(entry.getKey());
      GameProperties.Ore conf = entry.getValue();

      OreBehavior behavior = inferBehavior(oreId);
      defs.put(oreId, new OreDefinition(oreId, conf.getWeight(), conf.getValue(), behavior));
    }
  }

  public Optional<OreDefinition> find(String oreId) {
    return Optional.ofNullable(defs.get(canonical(oreId)));
  }

  public Collection<OreDefinition> all() {
    return defs.values();
  }

  public String canonical(String s) {
    return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
  }

  private OreBehavior inferBehavior(String oreId) {
    String id = oreId.toLowerCase(Locale.ROOT);
    if (id.contains("cratecredits")) return OreBehavior.INSTANT_CREDITS;
    if (id.contains("cratefuel")) return OreBehavior.INSTANT_FUEL;
    return OreBehavior.CARGO;
  }

  public OreDefinition weightedRandom(Random rnd) {
    List<OreDefinition> all = new ArrayList<>(defs.values());
    if (all.isEmpty()) {
      return null;
    }

    int total = all.stream().mapToInt(OreDefinition::weight).sum();
    if (total <= 0) {
      return all.get(rnd.nextInt(all.size()));
    }

    int roll = rnd.nextInt(total);
    for (OreDefinition d : all) {
      roll -= d.weight();
      if (roll < 0) return d;
    }
    return all.get(all.size() - 1);
  }
}
