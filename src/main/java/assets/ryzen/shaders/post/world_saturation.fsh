#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D SceneSampler;

layout(std140) uniform SaturationUniforms {
    vec4 SaturationParams;
};

const vec3 LUMA = vec3(0.2126, 0.7152, 0.0722);

void main() {
    vec3 color = texture(SceneSampler, uv).rgb;
    float luma = dot(color, LUMA);
    float maximum = max(color.r, max(color.g, color.b));
    float minimum = min(color.r, min(color.g, color.b));
    float pixelSaturation = maximum - minimum;
    float amount = SaturationParams.x;
    float factor = amount >= 1.0
            ? 1.0 + (amount - 1.0) * (1.0 - pixelSaturation * 0.5)
            : amount;
    finalColor = vec4(
        clamp(mix(vec3(luma), color, factor), 0.0, 1.0),
        1.0
    );
}
