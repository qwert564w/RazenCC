#version 330

in vec2 uv;
out vec4 finalColor;

uniform sampler2D texSampler;

layout(std140) uniform params {
    vec4 color;
};

void main() {
    vec4 sampleColor = texture(texSampler, uv);
    vec4 outputColor = sampleColor * color;
    if (outputColor.a < 0.01) {
        discard;
    }
    finalColor = outputColor;
}
