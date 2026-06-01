package particle;

import java.util.List;

import Serenity.Util.PhysicsConstants;

public final class ParticleStrongForce {
    private ParticleStrongForce() {
    }

    public static void accumulateAccelerations(List<Particle> particles, float[] ax, float[] ay, float[] az,
            float metersPerWorldUnit, float softeningLength, double attractionMultiplier,
            double repulsionMultiplier, double rangeMultiplier) {
        int count = particles.size();
        if (count < 2) {
            return;
        }

        if (ax.length < count || ay.length < count || az.length < count) {
            throw new IllegalArgumentException("Acceleration arrays must be at least particle count long");
        }

        double softeningSquared = softeningLength * softeningLength;
        double baseRange = PhysicsConstants.STRONG_RANGE_METERS * Math.max(0.05, rangeMultiplier);
        double distanceScale = metersPerWorldUnit;

        for (int i = 0; i < count - 1; i++) {
            Particle pi = particles.get(i);
            if (!isNucleon(pi)) {
                continue;
            }

            float pix = pi.position().x;
            float piy = pi.position().y;
            float piz = pi.position().z;

            for (int j = i + 1; j < count; j++) {
                Particle pj = particles.get(j);
                if (!isNucleon(pj)) {
                    continue;
                }

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
                double attractionTerm = PhysicsConstants.STRONG_ATTRACTION_COEFF
                        * Math.exp(-rMeters / baseRange) * attractionMultiplier;
                double repulsionTerm = PhysicsConstants.STRONG_REPULSION_COEFF
                        * Math.exp(-rMeters / (0.35 * baseRange)) * repulsionMultiplier;

                double signedForce = attractionTerm - repulsionTerm;
                if (signedForce == 0.0) {
                    continue;
                }

                double aiMeters = signedForce / pi.massKg();
                double ajMeters = -signedForce / pj.massKg();

                double aiWorld = clampSigned(aiMeters / metersPerWorldUnit, PhysicsConstants.MAX_STRONG_ACCEL_WORLD);
                double ajWorld = clampSigned(ajMeters / metersPerWorldUnit, PhysicsConstants.MAX_STRONG_ACCEL_WORLD);

                ax[i] += (float) (aiWorld * nx);
                ay[i] += (float) (aiWorld * ny);
                az[i] += (float) (aiWorld * nz);

                ax[j] += (float) (ajWorld * nx);
                ay[j] += (float) (ajWorld * ny);
                az[j] += (float) (ajWorld * nz);
            }
        }
    }

    private static boolean isNucleon(Particle particle) {
        return particle.type().isNucleon();
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
