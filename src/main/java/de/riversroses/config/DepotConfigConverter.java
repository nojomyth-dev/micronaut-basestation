package de.riversroses.config;

import java.util.Map;
import java.util.Optional;

import de.riversroses.config.GameProperties.Depot;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.TypeConverter;
import jakarta.inject.Singleton;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("rawtypes")
@Singleton
@NoArgsConstructor
@Data
class DepotConfigConverter implements TypeConverter<Map, Depot> {

  @Override
  public Optional<Depot> convert(Map object, Class<Depot> targetType, ConversionContext context) {
    return Optional.of(new Depot() {

      @SuppressWarnings("unchecked")
      @Override
      public String getId() {
        return (String) object.getOrDefault("id", "");
      }

      @SuppressWarnings("unchecked")
      @Override
      public String getName() {
        return (String) object.getOrDefault("name", "");
      }

      @Override
      public Double getX() {
        Object v = object.get("x");
        if (v == null)
          return 0D;
        if (v instanceof Number n)
          return n.doubleValue();
        if (v instanceof String s)
          return Double.parseDouble(s);
        return 0D;
      }

      @Override
      public Double getY() {
        Object v = object.get("y");
        if (v == null)
          return 0D;
        if (v instanceof Number n)
          return n.doubleValue();
        if (v instanceof String s)
          return Double.parseDouble(s);
        return 0D;
      }

      @Override
      public Double getCostPerFuel() {
        Object v = object.get("costPerFuel");
        if (v == null)
          return 0D;
        if (v instanceof Number n)
          return n.doubleValue();
        if (v instanceof String s)
          return Double.parseDouble(s);
        return 0D;
      }

      @Override
      public String toString() {
        return super.toString();
      }
    });
  }
}
