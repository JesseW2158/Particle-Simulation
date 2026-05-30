#version 400 core

in vec3 glowColor;

out vec4 fragColor;

void main() {
    vec2 d = gl_PointCoord - vec2(0.5);
    float r = length(d) * 2.0;       // 0 at center, 1 at edge
    if (r > 1.0) {
        discard;                     // round point, not square
    }
    float intensity = 1.0 - r;
    intensity = intensity * intensity; // soft glow falloff
    fragColor = vec4(glowColor * intensity, intensity);
}
