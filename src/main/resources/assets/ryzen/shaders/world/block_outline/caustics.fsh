#version 330 core

in vec2 uv;
out vec4 finalColor;

layout(std140) uniform BlockOutlineStyle {
    vec4 Tint;
    vec4 Params;
};

void main() {
    vec2 resolution = max(Params.xy, vec2(1.0));
    float minimum = min(resolution.x, resolution.y);
    vec2 coords = (uv * resolution * 2.0 - resolution) / minimum;
    float time = Params.z;
    float depth = -time * 0.5;
    float accumulator = 0.0;
    for (float index = 0.0; index < 8.0; index++) {
        accumulator += cos(index - depth - accumulator * coords.x);
        depth += sin(coords.y * index + accumulator);
    }
    depth += time * 0.5;
    vec3 color = vec3(
        cos(coords * vec2(depth, accumulator)) * 0.6 + 0.4,
        cos(accumulator + depth) * 0.5 + 0.5
    );
    color = cos(color * cos(vec3(depth, accumulator, 2.5)) * 0.5 + 0.5);
    finalColor = vec4(
        clamp(color * Params.w, 0.0, 1.0) * Tint.rgb,
        Tint.a
    );
}
