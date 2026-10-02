#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D CurrentInput;

layout(std140) uniform HandBlurUniforms {
    vec2 TexelSize;
    float Offset;
    float AlphaMultiplier;
};

void main() {
    vec2 delta = TexelSize * Offset;
    vec4 color = texture(CurrentInput, uv) * 4.0;
    color += texture(CurrentInput, uv + vec2(-delta.x, -delta.y));
    color += texture(CurrentInput, uv + vec2(delta.x, -delta.y));
    color += texture(CurrentInput, uv + vec2(-delta.x, delta.y));
    color += texture(CurrentInput, uv + vec2(delta.x, delta.y));
    color *= 0.125;
    finalColor = vec4(color.rgb, color.a * AlphaMultiplier);
}
