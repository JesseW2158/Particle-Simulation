package particle;

import org.joml.Vector3f;

import Serenity.Util.PhysicsConstants;

public enum ParticleType implements ParticleDefinition {
    ELECTRON(PhysicsConstants.ELECTRON_MASS_KG, PhysicsConstants.ELECTRON_CHARGE_E, new Vector3f(0.4f, 0.6f, 1.0f),
        new ParticleRenderStyle(0.8f, true, 0.65f, 1.9f)),
    PROTON(PhysicsConstants.PROTON_MASS_KG, PhysicsConstants.PROTON_CHARGE_E, new Vector3f(1.0f, 0.35f, 0.35f),
        new ParticleRenderStyle(2.4f, false, 0.0f, 1.0f)),
    NEUTRON(PhysicsConstants.NEUTRON_MASS_KG, PhysicsConstants.NEUTRON_CHARGE_E, new Vector3f(0.9f, 0.9f, 0.95f),
        new ParticleRenderStyle(2.5f, false, 0.0f, 1.0f));

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

    @Override
    public double mass() {
        return mass;
    }

    @Override
    public int charge() {
        return charge;
    }

    @Override
    public Vector3f color() {
        return color;
    }

    @Override
    public ParticleRenderStyle renderStyle() {
        return renderStyle;
    }

    @Override
    public String id() {
        return name();
    }

    @Override
    public boolean isNucleon() {
        return this == PROTON || this == NEUTRON;
    }
}
