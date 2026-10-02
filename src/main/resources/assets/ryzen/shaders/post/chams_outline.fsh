#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D MaskSampler;

layout(std140) uniform ChamsStyle {
    vec4 ChamsColor;
    vec4 ChamsParams;
};

void main() {
    float thickness = max(0.5, ChamsParams.z);
    vec2 texel = thickness / vec2(textureSize(MaskSampler, 0));
    float center = texture(MaskSampler, uv).a;
    float maximum = center;
    const vec2 directions[8] = vec2[8](
        vec2(1.0, 0.0), vec2(-1.0, 0.0),
        vec2(0.0, 1.0), vec2(0.0, -1.0),
        vec2(0.707, 0.707), vec2(-0.707, 0.707),
        vec2(0.707, -0.707), vec2(-0.707, -0.707)
    );
    for (int index = 0; index < 8; index++) {
        maximum = max(
            maximum,
            texture(MaskSampler, uv + directions[index] * texel).a
        );
    }
    float edge = smoothstep(0.05, 0.5, maximum - center);
    if (edge < 0.004) {
        discard;
    }
    finalColor = vec4(ChamsColor.rgb, edge * ChamsColor.a);
}
