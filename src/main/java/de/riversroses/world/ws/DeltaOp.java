package de.riversroses.world.ws;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public sealed interface DeltaOp permits
        ShipUpsert,
        ResourceUpsert,
        ResourceRemove,
        MissionUpsert,
        MissionRemove {
}
