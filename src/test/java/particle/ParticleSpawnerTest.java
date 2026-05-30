package particle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class ParticleSpawnerTest {
    @Test
    public void producesRequestedCount() {
        List<Particle> particles = ParticleSpawner.randomCloud(5000, 10f, 1L);
        assertEquals(5000, particles.size());
    }

    @Test
    public void allPositionsWithinBounds() {
        float h = 10f;
        List<Particle> particles = ParticleSpawner.randomCloud(2000, h, 1L);
        for (Particle p : particles) {
            assertTrue(p.position().x >= -h && p.position().x <= h);
            assertTrue(p.position().y >= -h && p.position().y <= h);
            assertTrue(p.position().z >= -h && p.position().z <= h);
        }
    }

    @Test
    public void reproducibleForSameSeed() {
        List<Particle> a = ParticleSpawner.randomCloud(100, 10f, 42L);
        List<Particle> b = ParticleSpawner.randomCloud(100, 10f, 42L);
        for (int i = 0; i < a.size(); i++) {
            assertEquals(a.get(i).position().x, b.get(i).position().x, 0f);
            assertEquals(a.get(i).type(), b.get(i).type());
        }
    }
}
