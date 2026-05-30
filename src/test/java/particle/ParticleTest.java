package particle;

import static org.junit.Assert.assertEquals;

import org.joml.Vector3f;
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
}
