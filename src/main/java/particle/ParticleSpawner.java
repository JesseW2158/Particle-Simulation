package particle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.joml.Vector3f;

public class ParticleSpawner {
    private ParticleSpawner() {
    }

    public static List<Particle> randomCloud(int count, float halfExtent, long seed) {
        return randomCloud(count, halfExtent, seed, Arrays.asList(ParticleType.values()));
    }

    public static List<Particle> randomCloud(int count, float halfExtent, long seed, List<ParticleDefinition> types) {
        Random rng = new Random(seed);
        if (types == null || types.isEmpty()) {
            throw new IllegalArgumentException("At least one particle definition is required");
        }

        List<Particle> particles = new ArrayList<>(count);
        int typeCount = types.size();
        for (int i = 0; i < count; i++) {
            float x = (rng.nextFloat() * 2f - 1f) * halfExtent;
            float y = (rng.nextFloat() * 2f - 1f) * halfExtent;
            float z = (rng.nextFloat() * 2f - 1f) * halfExtent;
            ParticleDefinition type = types.get(rng.nextInt(typeCount));
            particles.add(new Particle(new Vector3f(x, y, z), type));
        }
        return particles;
    }
}
