#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D BlurredSampler;

layout(std140) uniform PopChamsComposite {
    vec4 PopChamsParams;
};

void main() {
    vec4 blurred = texture(BlurredSampler, uv);
    float alpha = clamp(blurred.a * PopChamsParams.x, 0.0, 1.0);
    if (alpha <= 0.002) {
        discard;
    }
    finalColor = vec4(blurred.rgb * alpha, alpha);
}
