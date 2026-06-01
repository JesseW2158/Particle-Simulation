package particle;

import org.joml.Vector3f;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ParticleTest {
    private static final float EPS = 1e-4f;

    @Test
    public void typeMassesAndCharges() {
        assertEquals(1.0, ParticleType.ELECTRON.mass(), EPS);
        assertEquals(1836.0, ParticleType.PROTON.mass(), EPS);
        assertEquals(-1, ParticleType.ELECTRON.charge());
        assertEquals(1, ParticleType.PROTON.charge());
        assertEquals(0, ParticleType.NEUTRON.charge());
    }

    @Test
    public void cbrtMassScalesWithType() {
        Particle electron = new Particle(new Vector3f(0, 0, 0), ParticleType.ELECTRON);
        Particle proton = new Particle(new Vector3f(0, 0, 0), ParticleType.PROTON);
        assertEquals(1.0f, electron.cbrtMass(), EPS);
        assertEquals(12.245f, proton.cbrtMass(), 0.005f); // cbrt(1836) ~= 12.245
    }

    @Test
    public void renderStylesDifferentiateBodiesAndGlow() {
        Particle electron = new Particle(new Vector3f(0, 0, 0), ParticleType.ELECTRON);
        Particle proton = new Particle(new Vector3f(0, 0, 0), ParticleType.PROTON);
        Particle neutron = new Particle(new Vector3f(0, 0, 0), ParticleType.NEUTRON);

        assertEquals(0.8f, electron.renderRadius(), EPS);
        assertEquals(2.4f, proton.renderRadius(), EPS);
        assertEquals(2.5f, neutron.renderRadius(), EPS);
        assertEquals(0.65f, electron.glowStrength(), EPS);
        assertEquals(0.0f, proton.glowStrength(), EPS);
        assertEquals(1.9f, electron.glowRadiusScale(), EPS);
    }
}
