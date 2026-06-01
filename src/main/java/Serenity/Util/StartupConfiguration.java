package Serenity.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import particle.ParticleDefinition;

public record StartupConfiguration(SimulationSettings settings, List<ParticleDefinition> particleDefinitions) {
    public StartupConfiguration {
        if (settings == null) {
            throw new IllegalArgumentException("settings must not be null");
        }
        if (particleDefinitions == null || particleDefinitions.isEmpty()) {
            throw new IllegalArgumentException("particleDefinitions must not be empty");
        }
        particleDefinitions = Collections.unmodifiableList(new ArrayList<>(particleDefinitions));
    }
}
