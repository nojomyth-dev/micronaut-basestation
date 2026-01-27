package de.riversroses.world.ws;

import de.riversroses.ship.dto.ShipMarkerDto;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record ShipUpsert(ShipMarkerDto ship) implements DeltaOp {}
