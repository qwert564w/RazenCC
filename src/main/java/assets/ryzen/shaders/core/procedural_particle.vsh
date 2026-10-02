#version 330 core

layout(location = 0) in vec4 PosSize;
layout(location = 1) in vec4 Dynamics;
layout(location = 2) in vec4 Color;
layout(location = 3) in vec4 Params;

layout(std140) uniform Projection {
    mat4 ProjMat;
};

out vec2 particleUv;
flat out int particleShape;
out vec4 particleColor;
out float particleLife;
out float particleSeed;
out float particlePhase;
flat out float particleAdditivity;
flat out float particleGlow;

const int CORNER_MAP[6] = int[6](0, 1, 2, 0, 2, 3);
const vec2 CORNERS[4] = vec2[4](
    vec2(-1.0, -1.0),
    vec2(1.0, -1.0),
    vec2(1.0, 1.0),
    vec2(-1.0, 1.0)
);

void main() {
    vec2 corner = CORNERS[CORNER_MAP[gl_VertexID % 6]];
    float cosine = cos(Dynamics.x);
    float sine = sin(Dynamics.x);
    vec2 rotated = vec2(
        corner.x * cosine - corner.y * sine,
        corner.x * sine + corner.y * cosine
    );
    vec3 viewPosition = PosSize.xyz + vec3(rotated * PosSize.w, 0.0);
    gl_Position = ProjMat * vec4(viewPosition, 1.0);
    particleUv = corner;
    particleShape = int(Params.x + 0.5);
    particleColor = Color;
    particleLife = Dynamics.y;
    particleSeed = Dynamics.z;
    particlePhase = Dynamics.w;
    particleAdditivity = Params.y;
    particleGlow = Params.z;
}
