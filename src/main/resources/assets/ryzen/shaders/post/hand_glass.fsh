#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D SceneSampler;
uniform sampler2D MaskSampler;

layout(std140) uniform HandGlassUniforms {
    vec4 GlassColor;
    float Mirror;
};

void main() {
    float maskAlpha = texture(MaskSampler, uv).a;
    if (maskAlpha < 0.01) {
        discard;
    }

    vec2 sceneUv = uv;
    if (Mirror > 0.5) {
        sceneUv.x = 1.0 - sceneUv.x;
    }
    vec3 scene = texture(SceneSampler, sceneUv).rgb;
    float tintAmount = clamp(GlassColor.a, 0.0, 1.0) * 0.5;
    vec3 tinted = mix(scene, GlassColor.rgb, tintAmount);
    finalColor = vec4(clamp(tinted, 0.0, 1.0), clamp(maskAlpha, 0.0, 1.0));
}
