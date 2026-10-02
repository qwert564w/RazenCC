#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D MaskSampler;

layout(std140) uniform HandFillUniforms {
    vec4 FillColor;
    vec4 FillParams;
};

void main() {
    float maskAlpha = texture(MaskSampler, uv).a;
    if (maskAlpha < 0.01) {
        discard;
    }

    float alpha = clamp(maskAlpha, 0.0, 1.0) * FillColor.a;
    if (alpha < 0.002) {
        discard;
    }
    finalColor = vec4(FillColor.rgb, alpha);
}
