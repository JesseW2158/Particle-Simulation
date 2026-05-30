package particle;

import org.joml.Vector3f;

public enum ParticleType {
    ELECTRON(1.0, -1, new Vector3f(0.4f, 0.6f, 1.0f)),
    PROTON(1836.0, 1, new Vector3f(1.0f, 0.35f, 0.35f)),
    NEUTRON(1839.0, 0, new Vector3f(0.9f, 0.9f, 0.95f));

    private final double mass;
    private final int charge;
    private final Vector3f color;

    ParticleType(double mass, int charge, Vector3f color) {
        this.mass = mass;
        this.charge = charge;
        this.color = color;
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
}
