#version 400 core

layout(location = 0) in vec3 position;
layout(location = 1) in float cbrtMass;
layout(location = 2) in vec3 color;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform float k;

out vec3 glowColor;

void main() {
    vec4 clip = projectionMatrix * viewMatrix * vec4(position, 1.0);
    gl_Position = clip;
    gl_PointSize = max(k * cbrtMass / clip.w, 1.0);
    glowColor = color;
}
