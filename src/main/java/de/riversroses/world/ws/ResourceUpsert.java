package de.riversroses.world.ws;

import de.riversroses.world.dto.ResourceDto;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record ResourceUpsert(ResourceDto resource) implements DeltaOp {}
