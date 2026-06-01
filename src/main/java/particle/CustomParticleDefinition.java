package particle;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

public class CustomParticleDefinition implements ParticleDefinition {
    private final String id;
    private final double mass;
    private final int charge;
    private final Vector3f color;
    private final ParticleRenderStyle renderStyle;
    private final boolean nucleon;

    public CustomParticleDefinition(String id, double mass, int charge, Vector3f color,
            ParticleRenderStyle renderStyle, boolean nucleon) {
        this.id = id;
        this.mass = mass;
        this.charge = charge;
        this.color = new Vector3f(color);
        this.renderStyle = renderStyle;
        this.nucleon = nucleon;
    }

    @Override
    public String id() {
        return id;
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
        return new Vector3f(color);
    }

    @Override
    public ParticleRenderStyle renderStyle() {
        return renderStyle;
    }

    @Override
    public boolean isNucleon() {
        return nucleon;
    }

    public static CustomParticleDefinition from(ParticleDefinition definition) {
        return new CustomParticleDefinition(
                definition.id(),
                definition.mass(),
                definition.charge(),
                definition.color(),
                definition.renderStyle(),
                definition.isNucleon());
    }

    public static List<ParticleDefinition> defaultDefinitions() {
        List<ParticleDefinition> defaults = new ArrayList<>();
        defaults.add(from(ParticleType.PROTON));
        defaults.add(from(ParticleType.NEUTRON));
        defaults.add(from(ParticleType.ELECTRON));
        return defaults;
    }
}
