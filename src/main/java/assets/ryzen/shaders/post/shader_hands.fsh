#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D BlurredSampler;
uniform sampler2D MaskSampler;

layout(std140) uniform HandCompositeUniforms {
    vec4 FlameColor;
    float Time;
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

void main() {
    float maskAlpha = texture(MaskSampler, uv).a;
    if (maskAlpha > 0.85) {
        discard;
    }

    vec2 texel = 1.0 / vec2(textureSize(BlurredSampler, 0));
    float field =
          texture(BlurredSampler, uv).a * 0.5
        + texture(BlurredSampler, uv + vec2(texel.x, 0.0)).a * 0.125
        + texture(BlurredSampler, uv - vec2(texel.x, 0.0)).a * 0.125
        + texture(BlurredSampler, uv + vec2(0.0, texel.y)).a * 0.125
        + texture(BlurredSampler, uv - vec2(0.0, texel.y)).a * 0.125;
    if (field < 0.004) {
        discard;
    }

    float glow = pow(clamp(field, 0.0, 1.0), 1.0);
    float flicker = 0.92 + 0.08 * vnoise(vec2(Time * 3.0, uv.x * 9.0));
    vec3 outputColor = FlameColor.rgb * (1.0 + glow * 0.5);
    float intensity = glow * FlameColor.a * flicker;
    finalColor = vec4(outputColor, clamp(intensity, 0.0, 1.0));
}
