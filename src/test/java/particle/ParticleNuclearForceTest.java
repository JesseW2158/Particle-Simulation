package particle;

import java.util.List;

import org.joml.Vector3f;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ParticleNuclearForceTest {
    private static final float EPS = 1.0e-6f;

    @Test
    public void strongForceAttractsProtonAndNeutronAtShortRange() {
        Particle proton = new Particle(new Vector3f(-0.2f, 0f, 0f), ParticleType.PROTON);
        Particle neutron = new Particle(new Vector3f(0.2f, 0f, 0f), ParticleType.NEUTRON);

        float[] ax = new float[2];
        float[] ay = new float[2];
        float[] az = new float[2];

        ParticleStrongForce.accumulateAccelerations(List.of(proton, neutron), ax, ay, az,
                1.0e-15f, 0.05f, 1.0, 0.25, 1.0);

        assertTrue(ax[0] > 0f);
        assertTrue(ax[1] < 0f);
    }

    @Test
    public void strongForceDoesNotActOnElectronProtonPair() {
        Particle proton = new Particle(new Vector3f(-0.2f, 0f, 0f), ParticleType.PROTON);
        Particle electron = new Particle(new Vector3f(0.2f, 0f, 0f), ParticleType.ELECTRON);

        float[] ax = new float[2];
        float[] ay = new float[2];
        float[] az = new float[2];

        ParticleStrongForce.accumulateAccelerations(List.of(proton, electron), ax, ay, az,
                1.0e-15f, 0.05f, 1.0, 0.25, 1.0);

        assertEquals(0f, ax[0], EPS);
        assertEquals(0f, ax[1], EPS);
    }

    @Test
    public void weakForceCanBeScaledToZero() {
        Particle left = new Particle(new Vector3f(-0.2f, 0f, 0f), ParticleType.PROTON);
        Particle right = new Particle(new Vector3f(0.2f, 0f, 0f), ParticleType.NEUTRON);

        float[] ax = new float[2];
        float[] ay = new float[2];
        float[] az = new float[2];

        ParticleWeakForce.accumulateAccelerations(List.of(left, right), ax, ay, az,
                1.0e-15f, 0.05f, 0.0, 1.0);

        assertEquals(0f, ax[0], EPS);
        assertEquals(0f, ax[1], EPS);
    }
}
