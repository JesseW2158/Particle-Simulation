package particle;

public record ParticleRenderStyle(float sphereRadiusScale, boolean scaleWithCbrtMass, float glowStrength,
        float glowRadiusScale) {
    public float radiusFor(float cbrtMass) {
        return scaleWithCbrtMass ? sphereRadiusScale * cbrtMass : sphereRadiusScale;
    }
}