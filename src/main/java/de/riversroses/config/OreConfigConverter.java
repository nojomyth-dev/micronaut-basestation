package de.riversroses.config;

import java.util.Map;
import java.util.Optional;

import de.riversroses.config.GameProperties.Ore;
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
        return (Integer) object.getOrDefault("value", 0);
      }

      @SuppressWarnings("unchecked")
      @Override
      public Integer getWeight() {
        return (Integer) object.getOrDefault("weight", 0);
      }
    });
  }
}
