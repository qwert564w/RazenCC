#version 330 core

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in vec4 Color;

layout(std140) uniform Projection {
    mat4 ProjMat;
};

out vec2 uv;
out vec4 tint;

void main() {
    gl_Position = ProjMat * vec4(Position, 1.0);
    uv = UV0;
    tint = Color;
}
