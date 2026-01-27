package de.riversroses.api.dto.radar;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
@Serdeable
@Introspected
public class RadarScanResponse {
    List<FoundShip> ships;
    List<FoundResource> resources;

    @Value
    @Builder
    @Serdeable
    @Introspected
    public static class FoundShip {
        String shipId;
        String teamName;
        double x;
        double y;
        double speed;
        double heading;
    }

    @Value
    @Builder
    @Serdeable
    @Introspected
    public static class FoundResource {
        String id;
        String type;
        int value;
        double x;
        double y;
    }
}
