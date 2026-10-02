#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D MaskSampler;

layout(std140) uniform ChamsStyle {
    vec4 ChamsColor;
    vec4 ChamsParams;
};

float hash21(vec2 value) {
    value = fract(value * vec2(123.34, 345.45));
    value += dot(value, value + 34.345);
    return fract(value.x * value.y);
}

float noise(vec2 value) {
    vec2 cell = floor(value);
    vec2 local = fract(value);
    local = local * local * (3.0 - 2.0 * local);
    return mix(
        mix(hash21(cell), hash21(cell + vec2(1.0, 0.0)), local.x),
        mix(hash21(cell + vec2(0.0, 1.0)), hash21(cell + vec2(1.0)), local.x),
        local.y
    );
}

float fbm(vec2 value) {
    float result = 0.0;
    float amplitude = 0.5;
    for (int index = 0; index < 5; index++) {
        result += amplitude * noise(value);
        value *= 2.02;
        amplitude *= 0.5;
    }
    return result;
}

void main() {
    float mask = texture(MaskSampler, uv).a;
    if (mask < 0.01) {
        discard;
    }
    float time = ChamsParams.x;
    vec2 position = uv * vec2(3.0, 4.0);
    vec2 warp = vec2(
        fbm(position * 0.8 + vec2(time * 0.05, 0.0)),
        fbm(position * 0.8 + vec2(5.2, time * 0.04))
    );
    position += (warp - 0.5) * 2.2;
    float firstNoise = fbm(position * 1.2);
    float secondNoise = fbm(position * 2.6 + 4.0);
    float density = pow(
        smoothstep(0.30, 0.95, firstNoise * 0.7 + secondNoise * 0.3),
        1.4
    );
    float hue = fbm(position * 0.6 + 9.0);
    vec3 nebula = mix(
        ChamsColor.rgb * 0.55,
        ChamsColor.rgb * 1.7 + 0.2,
        smoothstep(0.18, 0.85, hue)
    );
    vec3 color = nebula * density * 1.3
            + ChamsColor.rgb * pow(firstNoise, 2.0) * 0.15;
    finalColor = vec4(
        clamp(color, 0.0, 1.0),
        clamp(mask * ChamsColor.a, 0.0, 1.0)
    );
}
