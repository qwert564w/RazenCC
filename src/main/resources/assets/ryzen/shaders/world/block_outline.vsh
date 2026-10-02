#version 330 core

layout(location = 0) in vec3 Position;

layout(std140) uniform BlockOutlineTransform {
    mat4 ModelViewProjection;
};

out vec2 uv;

vec2 signNotZero(vec2 value) {
    return vec2(value.x >= 0.0 ? 1.0 : -1.0, value.y >= 0.0 ? 1.0 : -1.0);
}

vec2 octahedralUv(vec3 position) {
    vec3 normal = position / max(
        abs(position.x) + abs(position.y) + abs(position.z),
        0.0001
    );
    vec2 mapped = normal.xy;
    if (normal.z < 0.0) {
        mapped = (1.0 - abs(mapped.yx)) * signNotZero(mapped);
    }
    mapped.y = -mapped.y;
    return mapped * 0.5 + 0.5;
}

void main() {
    uv = octahedralUv(Position * 2.0 - 1.0);
    gl_Position = ModelViewProjection * vec4(Position, 1.0);
}
