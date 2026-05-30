#version 400 core

layout(location = 0) in vec3 position;

out vec3 color;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 worldMatrix;

void main() {
    gl_Position = projectionMatrix * viewMatrix * worldMatrix * vec4(position, 1.0);
    color = vec3(position.x + 0.25, 0.17, position.y + 0.25);
}
