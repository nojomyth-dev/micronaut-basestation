package de.riversroses.world.event;

import de.riversroses.kernel.event.DomainEvent;
import de.riversroses.world.dto.WorldSnapshotResponse;

public record WorldSnapshotEvent(WorldSnapshotResponse snapshot) implements DomainEvent {
}
