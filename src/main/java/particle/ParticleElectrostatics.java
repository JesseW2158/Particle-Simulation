package particle;

import java.util.List;

import Serenity.Util.PhysicsConstants;

public final class ParticleElectrostatics {
    private ParticleElectrostatics() {
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
            float metersPerWorldUnit, float softeningLength, double electrostaticMultiplier) {
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
            double qi = pi.type().charge() * PhysicsConstants.ELEMENTARY_CHARGE_C;

            for (int j = i + 1; j < count; j++) {
                Particle pj = particles.get(j);
                double qj = pj.type().charge() * PhysicsConstants.ELEMENTARY_CHARGE_C;
                double chargeProduct = qi * qj;
                if (chargeProduct == 0.0) {
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

                double r2Meters = softenedR2World * distanceScaleSquared;
                double k = PhysicsConstants.COULOMB_CONSTANT * electrostaticMultiplier;
                double aiMeters = -k * chargeProduct / (pi.massKg() * r2Meters);
                double ajMeters = k * chargeProduct / (pj.massKg() * r2Meters);
                double aiWorld = clampSigned(aiMeters / metersPerWorldUnit,
                    PhysicsConstants.MAX_ELECTROSTATIC_ACCEL_WORLD);
                double ajWorld = clampSigned(ajMeters / metersPerWorldUnit,
                    PhysicsConstants.MAX_ELECTROSTATIC_ACCEL_WORLD);

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
