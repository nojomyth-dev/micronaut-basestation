package de.riversroses.world.ws;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record MissionRemove(String id) implements DeltaOp {}
