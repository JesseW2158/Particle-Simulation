package Serenity.Util;

public record SimulationSettings(int particleCount, float simulationSpeed, float spawnHalfExtent,
        float boundaryHalfExtent, float fixedPhysicsStepSeconds, float metersPerWorldUnit, float softeningLength,
        double gravityMultiplier, double electrostaticMultiplier,
        boolean strongForceEnabled, double strongAttractionMultiplier, double strongRepulsionMultiplier,
        double strongRangeMultiplier,
        boolean weakForceEnabled, double weakMultiplier, double weakRangeMultiplier) {
    public static SimulationSettings defaults() {
        return new SimulationSettings(
                400,
                0.2f,
                10.0f,
                12.0f,
                1.0f / 240.0f,
                1.0e-13f,
                0.25f,
                1.0,
                1.0,
                true,
                1.0,
                1.0,
                1.0,
                false,
                1.0,
                1.0);
    }
}