package de.riversroses.world.event;

import de.riversroses.kernel.event.DomainEvent;
import de.riversroses.world.ws.WorldDelta;

public record WorldDeltaEvent(WorldDelta delta) implements DomainEvent {
}
