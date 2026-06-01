package particle;

import org.joml.Vector3f;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import Serenity.Util.PhysicsConstants;

public class ParticleTest {
    private static final float EPS = 1e-4f;

    @Test
    public void typeMassesAndCharges() {
        assertEquals(PhysicsConstants.ELECTRON_MASS_KG, ParticleType.ELECTRON.mass(), 1.0e-40);
        assertEquals(PhysicsConstants.PROTON_MASS_KG, ParticleType.PROTON.mass(), 1.0e-35);
        assertEquals(PhysicsConstants.ELECTRON_CHARGE_E, ParticleType.ELECTRON.charge());
        assertEquals(PhysicsConstants.PROTON_CHARGE_E, ParticleType.PROTON.charge());
        assertEquals(PhysicsConstants.NEUTRON_CHARGE_E, ParticleType.NEUTRON.charge());
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

    @Test
    public void gravityPullsEqualMassesSymmetrically() {
        Particle left = new Particle(new Vector3f(-1f, 0f, 0f), ParticleType.PROTON);
        Particle right = new Particle(new Vector3f(1f, 0f, 0f), ParticleType.PROTON);

        ParticleGravity.step(java.util.List.of(left, right), 1.0f / 240.0f, 12f, 1.0e-13f, 0.25f);

        assertTrue(left.velocity().x > 0f);
        assertTrue(right.velocity().x < 0f);
        assertEquals(Math.abs(left.velocity().x), Math.abs(right.velocity().x), 1.0e-5f);
    }

    @Test
    public void electrostaticsAttractsOppositeCharges() {
        Particle left = new Particle(new Vector3f(-1f, 0f, 0f), ParticleType.PROTON);
        Particle right = new Particle(new Vector3f(1f, 0f, 0f), ParticleType.ELECTRON);

        ParticleElectrostatics.step(java.util.List.of(left, right), 1.0f / 240.0f, 12f, 1.0f, 0.25f);

        assertTrue(left.velocity().x > 0f);
        assertTrue(right.velocity().x < 0f);
    }

    @Test
    public void electrostaticsRepelsLikeCharges() {
        Particle left = new Particle(new Vector3f(-1f, 0f, 0f), ParticleType.PROTON);
        Particle right = new Particle(new Vector3f(1f, 0f, 0f), ParticleType.PROTON);

        ParticleElectrostatics.step(java.util.List.of(left, right), 1.0f / 240.0f, 12f, 1.0f, 0.25f);

        assertTrue(left.velocity().x < 0f);
        assertTrue(right.velocity().x > 0f);
    }

    @Test
    public void electrostaticsIgnoresNeutralParticle() {
        Particle proton = new Particle(new Vector3f(-1f, 0f, 0f), ParticleType.PROTON);
        Particle neutron = new Particle(new Vector3f(1f, 0f, 0f), ParticleType.NEUTRON);

        ParticleElectrostatics.step(java.util.List.of(proton, neutron), 1.0f / 240.0f, 12f, 1.0f, 0.25f);

        assertEquals(0f, proton.velocity().x, EPS);
        assertEquals(0f, neutron.velocity().x, EPS);
    }

    @Test
    public void borderBounceFlipsNormalVelocityAndKeepsSpeedMagnitude() {
        Particle particle = new Particle(new Vector3f(0f, 0f, 0f), ParticleType.PROTON);
        particle.velocity().set(2f, -3f, 4f);
        particle.position().set(20f, 0f, 0f);

        float speedBefore = particle.velocity().length();
        particle.bounceWithinCube(12f);
        float speedAfter = particle.velocity().length();

        assertEquals(-2f, particle.velocity().x, EPS);
        assertEquals(-3f, particle.velocity().y, EPS);
        assertEquals(4f, particle.velocity().z, EPS);
        assertEquals(speedBefore, speedAfter, EPS);
    }
}
