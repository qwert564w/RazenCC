#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D PrevSampler;
uniform sampler2D InjectSampler;

layout(std140) uniform HandTrailUniforms {
    vec2 TexelSize;
    float Decay;
    float Rise;
    float Time;
    float EmissionFloor;
};

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float vnoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int index = 0; index < 4; index++) {
        value += amplitude * vnoise(p);
        p = p * 2.0 + vec2(1.7, 9.2);
        amplitude *= 0.5;
    }
    return value;
}

void main() {
    vec2 flowUv = uv * vec2(6.0, 4.0);
    float side = fbm(flowUv + vec2(0.0, -Time * 1.4)) - 0.5;

    vec2 sourceUv = uv + vec2(side * TexelSize.x * 3.5, Rise * TexelSize.y);
    float advected = texture(PrevSampler, sourceUv).a * Decay;

    float coverage = texture(InjectSampler, uv).a;
    float band = smoothstep(0.15, 0.55, coverage);
    float tongues = smoothstep(
        0.42,
        0.85,
        fbm(flowUv * 1.6 + vec2(5.0, -Time * 2.2))
    );
    float emission = clamp(
        band * (EmissionFloor + (1.0 - EmissionFloor) * tongues) * 1.7,
        0.0,
        1.0
    );

    float value = max(emission, advected);
    finalColor = vec4(1.0, 1.0, 1.0, clamp(value, 0.0, 1.0));
}
