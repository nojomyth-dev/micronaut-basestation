package de.riversroses.world.ws;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record ResourceRemove(String id) implements DeltaOp {}
