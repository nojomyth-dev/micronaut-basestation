package de.riversroses.world.ore;

import de.riversroses.world.model.OreBehavior;

public record OreDefinition(
        String id,
        int weight,
        int value,
        OreBehavior behavior) {
}
