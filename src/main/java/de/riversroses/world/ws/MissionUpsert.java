package de.riversroses.world.ws;

import de.riversroses.world.dto.MissionDto;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record MissionUpsert(MissionDto mission) implements DeltaOp {}
