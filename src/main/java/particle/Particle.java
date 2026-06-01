package particle;

import org.joml.Vector3f;

public class Particle {
    private final Vector3f position;
    private final ParticleType type;

    public Particle(Vector3f position, ParticleType type) {
        this.position = position;
        this.type = type;
    }

    public Vector3f position() {
        return position;
    }

    public ParticleType type() {
        return type;
    }

    public float cbrtMass() {
        return (float) Math.cbrt(type.mass());
    }

    public float renderRadius() {
        return type.renderStyle().radiusFor(cbrtMass());
    }

    public float glowStrength() {
        return type.renderStyle().glowStrength();
    }

    public float glowRadiusScale() {
        return type.renderStyle().glowRadiusScale();
    }
}
