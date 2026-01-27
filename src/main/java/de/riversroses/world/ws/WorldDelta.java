package de.riversroses.world.ws;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public record WorldDelta(
    long tick,
    List<DeltaOp> ops
) {}
