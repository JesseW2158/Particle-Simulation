package fundamentalForces;

import particle.Particle;
import vector.Vector;

public class Gravity {
    private static final double G = 6.67430e-11;

    public static Vector gravitationalForce(Particle origin, Particle target) {
        double magnitude = gravityMagnitude(origin.mass(), target.mass(),
                Vector.subtract(target.position(), origin.position()).magnitude());
        Vector direction = Vector.subtract(target.position(), origin.position()).normalized();
        return direction.multiply(magnitude);
    }

    private static double gravityMagnitude(double mass1, double mass2, double distance) {
        return G * (mass1 * mass2) / Math.pow(distance / Math.pow(10, 9), 2);
    }
}