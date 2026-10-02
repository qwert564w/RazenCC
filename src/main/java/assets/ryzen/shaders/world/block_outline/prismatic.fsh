#version 330 core

in vec2 uv;
out vec4 finalColor;

layout(std140) uniform BlockOutlineStyle {
    vec4 Tint;
    vec4 Params;
};

const float TAU = 6.28318530718;

vec2 rotate(vec2 value, float angle) {
    float sine = sin(angle);
    float cosine = cos(angle);
    return mat2(cosine, -sine, sine, cosine) * value;
}

float hash21(vec2 value) {
    value = fract(value * vec2(123.34, 345.45));
    value += dot(value, value + 34.345);
    return fract(value.x * value.y);
}

vec3 palette(float value) {
    return 0.55 + 0.45 * cos(
        vec3(0.2, 1.1, 2.0) + value * vec3(1.15, 1.35, 1.55)
    );
}

float ribbons(vec2 position, float time, float warp) {
    float result = 0.0;
    float weight = 0.55;
    vec2 value = position;
    for (int index = 0; index < 7; index++) {
        float current = float(index);
        value = rotate(value, 0.45 + current * 0.23 + time * (0.05 + current * 0.004));
        value += 0.22 * vec2(
            sin(value.y * (2.1 + warp * 0.06) + time + current * 0.9),
            cos(value.x * (2.4 + warp * 0.05) - time * 1.1 - current * 0.6)
        );
        float line = 0.08 / (0.028 + abs(
            sin(value.x * (3.0 + warp * 0.03) + time * 1.4)
            + cos(value.y * (3.6 + warp * 0.025) - time * 1.1)
        ));
        result += (line + exp(-2.2 * dot(value, value)) * 0.42) * weight;
        value *= 1.34 + current * 0.015;
        value = rotate(value, -0.78 - current * 0.03);
        weight *= 0.66;
    }
    return result;
}

float sparkles(vec2 position, float time) {
    vec2 value = position * 4.0;
    vec2 cell = floor(value);
    vec2 local = fract(value) - 0.5;
    float result = 0.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 id = cell + vec2(x, y);
            float hash = hash21(id);
            vec2 offset = vec2(hash21(id + 1.3), hash21(id + 7.1)) - 0.5;
            float distanceToSpark = length(local - vec2(x, y) - offset * 0.7);
            float twinkle = 0.5 + 0.5 * sin(time * 2.0 + hash * TAU);
            result += smoothstep(0.16, 0.0, distanceToSpark) * twinkle * hash;
        }
    }
    return result;
}

void main() {
    vec2 resolution = max(Params.xy, vec2(1.0));
    vec2 position = (uv * resolution * 2.0 - resolution) / resolution.y;
    float time = Params.z * 0.45;
    float radial = length(position);
    vec2 drift = 0.16 * vec2(
        sin(time + position.y * 3.0),
        cos(time * 0.8 - position.x * 3.6)
    );
    vec2 value = rotate(position + drift, time * 0.08);
    float farField = ribbons(value * 0.72, time * 0.72, 1.0);
    float midField = ribbons(value * 1.12 + farField * 0.014, time, 2.0);
    float nearField = ribbons(value * 1.95 + midField * 0.02, time * 1.18, 3.0);
    float glow = exp(-2.4 * radial * radial);
    float ring = pow(max(0.0, cos(radial * 9.5 - time * 1.55 + nearField * 0.055)), 7.0);
    float stars = sparkles(value + nearField * 0.018, time) * (0.4 + glow);
    vec3 color = palette(farField * 0.032 - time * 0.10 + 0.8) * 0.24;
    color += palette(midField * 0.04 - time * 0.13 + 1.9) * (0.32 + glow * 0.75);
    color += palette(nearField * 0.05 - time * 0.16 + 3.1) * (0.30 + ring * 0.45);
    color += vec3(1.0, 0.97, 0.92) * stars * 0.42;
    color *= smoothstep(1.45, 0.15, radial);
    color = 1.0 - exp(-color * (1.35 + nearField * 0.08));
    color = pow(clamp(color, 0.0, 1.0), vec3(0.82));
    finalColor = vec4(
        clamp(color * Params.w, 0.0, 1.0) * Tint.rgb,
        Tint.a
    );
}
