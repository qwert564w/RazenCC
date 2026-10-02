#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D MaskSampler;

layout(std140) uniform HandFillUniforms {
    vec4 FillColor;
    vec4 FillParams;
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
    float a = hash21(cell);
    float b = hash21(cell + vec2(1.0, 0.0));
    float c = hash21(cell + vec2(0.0, 1.0));
    float d = hash21(cell + vec2(1.0));
    return mix(mix(a, b, local.x), mix(c, d, local.x), local.y);
}

void main() {
    float mask = texture(MaskSampler, uv).a;
    if (mask < 0.01) {
        discard;
    }

    float time = FillParams.x;
    vec2 position = uv * vec2(6.0, 10.0);
    float firstWarp = noise(position * 0.6 + vec2(time * 0.45, -time * 0.6));
    float secondWarp = noise(
        position * 1.3 + firstWarp * 2.2 + vec2(-time * 0.35, time * 0.5)
    );
    float value = noise(position + secondWarp * 2.4 + vec2(time * 0.2, -time * 0.7));
    float energy = smoothstep(0.15, 0.95, value);
    float vein = pow(energy, 2.5);
    vec3 color = mix(FillColor.rgb * 0.3, FillColor.rgb * 1.7, energy);
    color += FillColor.rgb * vein * 0.9;
    finalColor = vec4(
        clamp(color, 0.0, 1.0),
        clamp(mask * FillColor.a, 0.0, 1.0)
    );
}
