package de.riversroses.world.config;

import java.util.Map;
import java.util.Optional;
import de.riversroses.kernel.engine.GameProperties.Ore;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.TypeConverter;
import jakarta.inject.Singleton;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("rawtypes")
@Singleton
@NoArgsConstructor
@Data
class OreConfigConverter implements TypeConverter<Map, Ore> {
  @Override
  public Optional<Ore> convert(Map object, Class<Ore> targetType, ConversionContext context) {
    return Optional.of(new Ore() {
      @SuppressWarnings("unchecked")
      @Override
      public Integer getValue() {
        Object v = object.getOrDefault("value", 0);
        if (v instanceof Number n)
          return n.intValue();
        if (v instanceof String s)
          return Integer.parseInt(s);
        return 0;
      }

      @SuppressWarnings("unchecked")
      @Override
      public Integer getWeight() {
        Object v = object.getOrDefault("weight", 0);
        if (v instanceof Number n)
          return n.intValue();
        if (v instanceof String s)
          return Integer.parseInt(s);
        return 0;
      }
    });
  }
}
