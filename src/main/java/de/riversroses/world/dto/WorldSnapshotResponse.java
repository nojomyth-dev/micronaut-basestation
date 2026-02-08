package de.riversroses.world.dto;

import de.riversroses.ship.dto.ShipMarkerDto;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;

@Serdeable
@Introspected
public record WorldSnapshotResponse(
        Double width,
        Double height,
        Double homeX,
        Double homeY,
        Double refillRadius,
        List<ShipMarkerDto> ships,
        List<StationDto> stations,
        List<MissionDto> missions,
        List<ResourceDto> resources) {
}
