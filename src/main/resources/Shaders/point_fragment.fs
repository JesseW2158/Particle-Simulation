#version 400 core

in vec3 particleColor;
in float particleGlowStrength;
in float particleGlowRadiusScale;

uniform int renderPass;

out vec4 fragColor;

void main() {
    vec2 centered = gl_PointCoord * 2.0 - vec2(1.0);
    float radial = length(centered);
    if (radial > 1.0) {
        discard;
    }

    float glowRadiusScale = max(particleGlowRadiusScale, 1.0);
    float coreRadius = 1.0 / glowRadiusScale;
    bool hasGlow = particleGlowStrength > 0.0;

    if (renderPass == 0) {
        if (hasGlow && radial > coreRadius) {
            discard;
        }

        vec2 sphereCoord = hasGlow ? centered / coreRadius : centered;
        float sphereR2 = dot(sphereCoord, sphereCoord);
        if (sphereR2 > 1.0) {
            discard;
        }

        float z = sqrt(max(1.0 - sphereR2, 0.0));
        vec3 normal = normalize(vec3(sphereCoord.x, -sphereCoord.y, z));
        vec3 lightDir = normalize(vec3(-0.45, 0.6, 0.65));
        float ambient = 0.28;
        float diffuse = max(dot(normal, lightDir), 0.0);
        float rim = pow(1.0 - max(normal.z, 0.0), 2.2);
        vec3 shadedColor = particleColor * (ambient + 0.72 * diffuse) + vec3(1.0) * (0.10 * rim);
        fragColor = vec4(shadedColor, 1.0);
        return;
    }

    if (!hasGlow || radial <= coreRadius) {
        discard;
    }

    float haloSpan = max(1.0 - coreRadius, 0.0001);
    float haloT = clamp((radial - coreRadius) / haloSpan, 0.0, 1.0);
    float haloAlpha = particleGlowStrength * pow(1.0 - haloT, 2.8);
    vec3 haloColor = particleColor * (1.0 + 0.35 * (1.0 - haloT));
    fragColor = vec4(haloColor * haloAlpha, haloAlpha);
}
