package de.riversroses.application.simulation;

import de.riversroses.domain.model.Ship;
import java.util.function.Consumer;

public record GameCommand(String shipId, Consumer<Ship> action) {
}
