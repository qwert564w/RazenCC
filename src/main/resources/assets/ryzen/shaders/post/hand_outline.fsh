#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D MaskSampler;

layout(std140) uniform HandOutlineUniforms {
    vec4 OutlineColor;
    float Thickness;
};

void main() {
    float thickness = max(0.5, Thickness);
    vec2 texel = thickness / vec2(textureSize(MaskSampler, 0));

    float center = texture(MaskSampler, uv).a;
    float minimumAlpha = center;
    float maximumAlpha = center;
    const vec2 directions[8] = vec2[8](
        vec2(1.0, 0.0), vec2(-1.0, 0.0), vec2(0.0, 1.0), vec2(0.0, -1.0),
        vec2(0.707, 0.707), vec2(-0.707, 0.707),
        vec2(0.707, -0.707), vec2(-0.707, -0.707)
    );
    for (int index = 0; index < 8; index++) {
        float alpha = texture(MaskSampler, uv + directions[index] * texel).a;
        minimumAlpha = min(minimumAlpha, alpha);
        maximumAlpha = max(maximumAlpha, alpha);
    }

    float edge = smoothstep(0.05, 0.5, maximumAlpha - minimumAlpha);
    float alpha = edge * OutlineColor.a;
    if (alpha < 0.004) {
        discard;
    }
    finalColor = vec4(OutlineColor.rgb, alpha);
}
