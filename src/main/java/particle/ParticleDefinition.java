package particle;

import org.joml.Vector3f;

public interface ParticleDefinition {
    String id();

    double mass();

    int charge();

    Vector3f color();

    ParticleRenderStyle renderStyle();

    boolean isNucleon();
}
