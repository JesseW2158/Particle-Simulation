package Serenity.Util;

public final class PhysicsConstants {
    public static final double GRAVITATIONAL_CONSTANT = 6.67430e-11;
    public static final double COULOMB_CONSTANT = 8.9875517923e9;

    public static final double ELEMENTARY_CHARGE_C = 1.602176634e-19;

    public static final double ELECTRON_MASS_KG = 9.1093837015e-31;
    public static final double PROTON_MASS_KG = 1.67262192369e-27;
    public static final double NEUTRON_MASS_KG = 1.67492749804e-27;

    public static final int ELECTRON_CHARGE_E = -1;
    public static final int PROTON_CHARGE_E = 1;
    public static final int NEUTRON_CHARGE_E = 0;

    // Limit electrostatic acceleration in world units to avoid numerical blow-up.
    public static final double MAX_ELECTROSTATIC_ACCEL_WORLD = 5000.0;

    // Strong-force approximation constants.
    public static final double STRONG_ATTRACTION_COEFF = 6.0e-16;
    public static final double STRONG_REPULSION_COEFF = 2.2e-15;
    public static final double STRONG_RANGE_METERS = 1.4e-15;

    // Weak-force placeholder constants.
    public static final double WEAK_FORCE_COEFF = 1.5e-17;
    public static final double WEAK_RANGE_METERS = 2.0e-16;

    public static final double MAX_STRONG_ACCEL_WORLD = 3000.0;
    public static final double MAX_WEAK_ACCEL_WORLD = 1200.0;

    private PhysicsConstants() {
    }
}