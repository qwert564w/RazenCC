#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D RawMask;

layout(std140) uniform HandMaskUniforms {
    vec2 TexelSize;
    vec2 GlowSides;
};

float allowedSide(vec2 coord) {
    float left = 1.0 - smoothstep(0.54, 0.68, coord.x);
    float right = smoothstep(0.32, 0.46, coord.x);
    return clamp(GlowSides.x * left + GlowSides.y * right, 0.0, 1.0);
}

// Weighting pass over the raw mask: same 3x3 kernel as before, but each
// tap is a single cached read instead of a 4-texture comparison.
void main() {
    vec4 center = texture(RawMask, uv);
    float axis = texture(RawMask, uv + vec2(TexelSize.x, 0.0)).a;
    axis += texture(RawMask, uv - vec2(TexelSize.x, 0.0)).a;
    axis += texture(RawMask, uv + vec2(0.0, TexelSize.y)).a;
    axis += texture(RawMask, uv - vec2(0.0, TexelSize.y)).a;
    float diagonal = texture(RawMask, uv + TexelSize).a;
    diagonal += texture(RawMask, uv - TexelSize).a;
    diagonal += texture(RawMask, uv + vec2(TexelSize.x, -TexelSize.y)).a;
    diagonal += texture(RawMask, uv + vec2(-TexelSize.x, TexelSize.y)).a;

    float mask = center.a * 0.58 + axis * 0.085 + diagonal * 0.025;
    mask = smoothstep(0.02, 0.72, mask);
    mask *= allowedSide(uv);

    finalColor = vec4(center.rgb, clamp(mask, 0.0, 1.0));
}
