#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D BlurredSampler;
uniform sampler2D MaskSampler;

layout(std140) uniform ChamsStyle {
    vec4 ChamsColor;
    vec4 ChamsParams;
};

void main() {
    float mask = texture(MaskSampler, uv).a;
    if (mask > 0.45) {
        discard;
    }
    float glow = max(0.0, texture(BlurredSampler, uv).a - mask);
    glow = pow(clamp(glow, 0.0, 1.0), 0.72);
    float alpha = clamp(glow * ChamsParams.w * ChamsColor.a, 0.0, 1.0);
    if (alpha < 0.003) {
        discard;
    }
    finalColor = vec4(ChamsColor.rgb * (1.0 + glow * 0.45), alpha);
}
