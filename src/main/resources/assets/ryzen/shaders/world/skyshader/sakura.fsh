#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D DepthSampler;

#moj_import <ryzen:skyshader_common.glsl>

float petal(vec2 p, float angle) {
    float c = cos(angle);
    float s = sin(angle);
    p = mat2(c, -s, s, c) * p;
    p.y += abs(p.x) * 0.22;
    float shape = length(p / vec2(0.24, 0.48));
    return smoothstep(1.0, 0.72, shape);
}

// One drifting petal per grid cell over a dusk gradient; the cell seed drives
// its sway, fall phase and tint so no two petals move alike.
void main() {
    if (!isSkyDepth(texture(DepthSampler, uv).r)) {
        discard;
    }
    vec3 direction = skyDirection(uv);
    vec2 plane = skyPlane(direction);
    float t = SkyParams.x * SkyParams.y * 0.16;

    vec3 color = mix(
        vec3(0.94, 0.58, 0.72),
        vec3(0.28, 0.12, 0.42),
        smoothstep(-0.25, 0.85, direction.y)
    );
    color += vec3(0.25, 0.08, 0.18) * sin((plane.x + plane.y) * 3.0 + t * 0.15) * 0.12;

    vec2 gridUv = plane * (7.0 * SkyParams.w);
    vec2 cell = floor(gridUv);
    vec2 local = fract(gridUv) - 0.5;
    float seed = skyHash(cell);
    local.x += sin(t + seed * 18.0) * 0.28;
    local.y += fract(t * (0.16 + seed * 0.12) + seed) - 0.5;

    float flower = petal(local, seed * 6.28318 + t * 0.15);
    vec3 petalColor = mix(vec3(1.0, 0.72, 0.82), vec3(1.0, 0.94, 0.97), seed);
    color = mix(color, petalColor, flower * (0.55 + seed * 0.35));
    finalColor = vec4(color, 1.0);
}
