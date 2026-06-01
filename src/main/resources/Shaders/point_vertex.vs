#version 400 core

layout(location = 0) in vec3 position;
layout(location = 1) in float sphereRadius;
layout(location = 2) in vec3 color;
layout(location = 3) in vec2 glowParams;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform float k;

out vec3 particleColor;
out float particleGlowStrength;
out float particleGlowRadiusScale;

void main() {
    vec4 clip = projectionMatrix * viewMatrix * vec4(position, 1.0);
    gl_Position = clip;
    float renderRadius = sphereRadius * max(glowParams.y, 1.0);
    gl_PointSize = max(k * renderRadius / clip.w, 1.0);
    particleColor = color;
    particleGlowStrength = glowParams.x;
    particleGlowRadiusScale = glowParams.y;
}
