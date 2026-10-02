#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D BlurredSampler;
uniform sampler2D MaskSampler;

layout(std140) uniform ChamsStyle {
    vec4 ChamsColor;
    vec4 ChamsParams;
};

float hash21(vec2 value) {
    value = fract(value * vec2(123.34, 456.21));
    value += dot(value, value + 45.32);
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
    float amplitude = 0.55;
    for (int index = 0; index < 5; index++) {
        result += amplitude * noise(value);
        value = value * 2.07 + vec2(13.0, 7.0);
        amplitude *= 0.48;
    }
    return result;
}

void main() {
    float mask = texture(MaskSampler, uv).a;
    float silhouette = smoothstep(0.1, 0.55, mask);
    if (silhouette < 0.005) {
        discard;
    }
    float blurred = texture(BlurredSampler, uv).a;
    float time = ChamsParams.x;
    vec2 fireUv = vec2(
        uv.x * 35.0 + sin(time * 0.04 + uv.y * 6.0) * 0.6,
        uv.y * 18.0 - time * 0.8
    );
    float turbulence = fbm(fireUv) * 0.65
            + fbm(fireUv * 2.3 + vec2(time * 0.4, 0.0)) * 0.35;
    float depth = smoothstep(0.5, 1.0, blurred);
    float fresnel = 1.0 - depth;
    float heat = clamp(
        fresnel * (0.55 + turbulence * 0.85)
            + turbulence * 0.4 * (1.0 - depth * 0.4),
        0.0,
        1.4
    );
    vec3 hot = mix(ChamsColor.rgb, vec3(1.0, 0.92, 0.55), 0.55) * 1.3;
    vec3 middle = ChamsColor.rgb * 1.15;
    vec3 cool = ChamsColor.rgb * 0.45;
    vec3 fireColor = heat > 0.65
            ? mix(middle, hot, smoothstep(0.65, 1.1, heat))
            : mix(cool, middle, smoothstep(0.0, 0.65, heat));
    float flame = smoothstep(0.28, 0.72, turbulence);
    float alpha = silhouette * max(
        fresnel * (0.4 + heat * 0.6),
        flame * (0.25 + heat * 0.5) * (1.0 - depth * 0.4)
    );
    finalColor = vec4(fireColor, clamp(alpha * ChamsColor.a, 0.0, 0.85));
}
