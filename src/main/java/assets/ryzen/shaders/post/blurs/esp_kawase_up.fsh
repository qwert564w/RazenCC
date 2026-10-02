#version 330 core

in vec2 uv;
out vec4 color;

uniform sampler2D CurrentInput;

layout(std140) uniform KawaseUpUniforms {
    vec2 HalfPixel;
    vec2 Padding;
    float Offset;
};

void main() {
    vec2 hp = HalfPixel * Offset;

    vec4 sum = texture(CurrentInput, uv + vec2(-hp.x * 2.0, 0.0));
    sum += texture(CurrentInput, uv + vec2(-hp.x, hp.y)) * 2.0;
    sum += texture(CurrentInput, uv + vec2(0.0, hp.y * 2.0));
    sum += texture(CurrentInput, uv + vec2(hp.x, hp.y)) * 2.0;
    sum += texture(CurrentInput, uv + vec2(hp.x * 2.0, 0.0));
    sum += texture(CurrentInput, uv + vec2(hp.x, -hp.y)) * 2.0;
    sum += texture(CurrentInput, uv + vec2(0.0, -hp.y * 2.0));
    sum += texture(CurrentInput, uv + vec2(-hp.x, -hp.y)) * 2.0;

    color = sum * 0.0833;
}
