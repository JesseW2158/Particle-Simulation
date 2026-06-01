package particle;

import org.joml.Vector3f;

public enum ParticleType {
    ELECTRON(1.0, -1, new Vector3f(0.4f, 0.6f, 1.0f), new ParticleRenderStyle(0.8f, true, 0.65f, 1.9f)),
    PROTON(1836.0, 1, new Vector3f(1.0f, 0.35f, 0.35f), new ParticleRenderStyle(2.4f, false, 0.0f, 1.0f)),
    NEUTRON(1839.0, 0, new Vector3f(0.9f, 0.9f, 0.95f), new ParticleRenderStyle(2.5f, false, 0.0f, 1.0f));

    private final double mass;
    private final int charge;
    private final Vector3f color;
    private final ParticleRenderStyle renderStyle;

    ParticleType(double mass, int charge, Vector3f color, ParticleRenderStyle renderStyle) {
        this.mass = mass;
        this.charge = charge;
        this.color = color;
        this.renderStyle = renderStyle;
    }

    public double mass() {
        return mass;
    }

    public int charge() {
        return charge;
    }

    public Vector3f color() {
        return color;
    }

    public ParticleRenderStyle renderStyle() {
        return renderStyle;
    }
}
