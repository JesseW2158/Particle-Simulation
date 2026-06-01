package particle;

import java.util.List;

import Serenity.Util.PhysicsConstants;

public final class ParticleWeakForce {
    private ParticleWeakForce() {
    }

    public static void accumulateAccelerations(List<Particle> particles, float[] ax, float[] ay, float[] az,
            float metersPerWorldUnit, float softeningLength, double weakMultiplier, double rangeMultiplier) {
        int count = particles.size();
        if (count < 2) {
            return;
        }

        if (ax.length < count || ay.length < count || az.length < count) {
            throw new IllegalArgumentException("Acceleration arrays must be at least particle count long");
        }

        double softeningSquared = softeningLength * softeningLength;
        double baseRange = PhysicsConstants.WEAK_RANGE_METERS * Math.max(0.05, rangeMultiplier);
        double distanceScale = metersPerWorldUnit;

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

                double rMeters = Math.sqrt(softenedR2World) * distanceScale;
                double interaction = PhysicsConstants.WEAK_FORCE_COEFF
                        * Math.exp(-rMeters / baseRange) * weakMultiplier;
                if (interaction == 0.0) {
                    continue;
                }

                double aiMeters = interaction / pi.massKg();
                double ajMeters = -interaction / pj.massKg();

                double aiWorld = clampSigned(aiMeters / metersPerWorldUnit, PhysicsConstants.MAX_WEAK_ACCEL_WORLD);
                double ajWorld = clampSigned(ajMeters / metersPerWorldUnit, PhysicsConstants.MAX_WEAK_ACCEL_WORLD);

                ax[i] += (float) (aiWorld * nx);
                ay[i] += (float) (aiWorld * ny);
                az[i] += (float) (aiWorld * nz);

                ax[j] += (float) (ajWorld * nx);
                ay[j] += (float) (ajWorld * ny);
                az[j] += (float) (ajWorld * nz);
            }
        }
    }

    private static double clampSigned(double value, double maxAbs) {
        if (value > maxAbs) {
            return maxAbs;
        }
        if (value < -maxAbs) {
            return -maxAbs;
        }
        return value;
    }
}
