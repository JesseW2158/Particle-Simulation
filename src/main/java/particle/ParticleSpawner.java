package particle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.joml.Vector3f;

public class ParticleSpawner {
    private ParticleSpawner() {
    }

    public static List<Particle> randomCloud(int count, float halfExtent, long seed) {
        Random rng = new Random(seed);
        ParticleType[] types = ParticleType.values();
        List<Particle> particles = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            float x = (rng.nextFloat() * 2f - 1f) * halfExtent;
            float y = (rng.nextFloat() * 2f - 1f) * halfExtent;
            float z = (rng.nextFloat() * 2f - 1f) * halfExtent;
            ParticleType type = types[rng.nextInt(types.length)];
            particles.add(new Particle(new Vector3f(x, y, z), type));
        }
        return particles;
    }
}
