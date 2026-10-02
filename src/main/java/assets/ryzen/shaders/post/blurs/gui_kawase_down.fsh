#version 330 core

in vec2 uv;
out vec4 color;

uniform sampler2D CurrentInput;

layout(std140) uniform GuiKawaseUniforms {
    vec2 HalfPixel;
    vec2 Padding;
    float Offset;
};

void main() {
    vec2 hp = HalfPixel * Offset;

    vec4 sum = texture(CurrentInput, uv) * 4.0;
    sum += texture(CurrentInput, uv - hp);
    sum += texture(CurrentInput, uv + hp);
    sum += texture(CurrentInput, uv + vec2(hp.x, -hp.y));
    sum += texture(CurrentInput, uv - vec2(hp.x, -hp.y));

    color = sum * 0.125;
}
