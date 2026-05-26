package fundamentalForces;

import particle.Particle;
import vector.Vector;

public class Gravity {
    private static final double G = 6.67430e-11;

    public static Vector gravitationalForce(Particle origin, Particle target) {
        double magnitude = gravityMagnitude(origin.mass(), target.mass(),
                origin.position().subtract(target.position()));
        Vector direction = target.position().subtract(origin.position()).normalize();
        return direction.multiply(magnitude);
    }

    private static double gravityMagnitude(double mass1, double mass2, double distance) {
        return G * (mass1 * mass2) / Math.pow(distance / Math.pow(mass2, distance), 2);
    }
}