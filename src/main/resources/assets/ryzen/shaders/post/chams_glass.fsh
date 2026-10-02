#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D SceneSampler;
uniform sampler2D MaskSampler;

layout(std140) uniform ChamsStyle {
    vec4 ChamsColor;
    vec4 ChamsParams;
};

void main() {
    float mask = texture(MaskSampler, uv).a;
    if (mask < 0.01) {
        discard;
    }
    vec2 texel = 1.0 / vec2(textureSize(MaskSampler, 0));
    vec2 offset = texel * 2.5;
    vec2 gradient = vec2(
        texture(MaskSampler, uv + vec2(offset.x, 0.0)).a
            - texture(MaskSampler, uv - vec2(offset.x, 0.0)).a,
        texture(MaskSampler, uv + vec2(0.0, offset.y)).a
            - texture(MaskSampler, uv - vec2(0.0, offset.y)).a
    );
    vec2 refractedUv = uv - gradient * 0.10;
    if (ChamsParams.y > 0.5) {
        refractedUv.x = 1.0 - refractedUv.x;
    }
    vec3 scene = texture(SceneSampler, refractedUv).rgb;
    vec3 glass = mix(
        scene,
        ChamsColor.rgb,
        clamp(ChamsColor.a, 0.0, 1.0) * 0.4
    );
    finalColor = vec4(clamp(glass, 0.0, 1.0), clamp(mask, 0.0, 1.0));
}
