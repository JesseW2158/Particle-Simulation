package particle;

import java.util.List;

import Serenity.Util.PhysicsConstants;

public final class ParticleGravity {
    private ParticleGravity() {
    }

    public static void step(List<Particle> particles, float timestepSeconds, float boundaryHalfExtent,
            float metersPerWorldUnit, float softeningLength) {
        int count = particles.size();
        if (count < 2) {
            return;
        }

        float[] ax = new float[count];
        float[] ay = new float[count];
        float[] az = new float[count];

        accumulateAccelerations(particles, ax, ay, az, metersPerWorldUnit, softeningLength);

        for (int i = 0; i < count; i++) {
            particles.get(i).integrate(ax[i], ay[i], az[i], timestepSeconds);
            particles.get(i).bounceWithinCube(boundaryHalfExtent);
        }
    }

    public static void accumulateAccelerations(List<Particle> particles, float[] ax, float[] ay, float[] az,
            float metersPerWorldUnit, float softeningLength) {
        accumulateAccelerations(particles, ax, ay, az, metersPerWorldUnit, softeningLength, 1.0);
    }

    public static void accumulateAccelerations(List<Particle> particles, float[] ax, float[] ay, float[] az,
            float metersPerWorldUnit, float softeningLength, double gravityMultiplier) {
        int count = particles.size();
        if (count < 2) {
            return;
        }

        if (ax.length < count || ay.length < count || az.length < count) {
            throw new IllegalArgumentException("Acceleration arrays must be at least particle count long");
        }

        double softeningSquared = softeningLength * softeningLength;
        double distanceScaleSquared = metersPerWorldUnit * metersPerWorldUnit;

        for (int i = 0; i < count - 1; i++) {
            Particle pi = particles.get(i);
            float pix = pi.position().x;
            float piy = pi.position().y;
            float piz = pi.position().z;
            for (int j = i + 1; j < count; j++) {
                Particle pj = particles.get(j);
                double dx = pj.position().x - pix;
                double dy = pj.position().y - piy;
                double dz = pj.position().z - piz;

                double r2World = dx * dx + dy * dy + dz * dz;
                double softenedR2World = Math.max(r2World, softeningSquared);
                double inverseR = 1.0 / Math.sqrt(softenedR2World);

                double nx = dx * inverseR;
                double ny = dy * inverseR;
                double nz = dz * inverseR;

                double r2Meters = softenedR2World * distanceScaleSquared;
                double g = PhysicsConstants.GRAVITATIONAL_CONSTANT * gravityMultiplier;
                double aiMeters = g * pj.massKg() / r2Meters;
                double ajMeters = g * pi.massKg() / r2Meters;
                double aiWorld = aiMeters / metersPerWorldUnit;
                double ajWorld = ajMeters / metersPerWorldUnit;

                ax[i] += (float) (aiWorld * nx);
                ay[i] += (float) (aiWorld * ny);
                az[i] += (float) (aiWorld * nz);

                ax[j] -= (float) (ajWorld * nx);
                ay[j] -= (float) (ajWorld * ny);
                az[j] -= (float) (ajWorld * nz);
            }
        }
    }
}