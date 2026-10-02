#version 330 core

in vec2 uv;
out vec4 finalColor;

layout(std140) uniform BlockOutlineStyle {
    vec4 Tint;
    vec4 Params;
};

void mainImage(out vec4 color, vec2 fragCoord) {
    vec2 resolution = max(Params.xy, vec2(1.0));
    vec2 position = 0.2 * ((fragCoord + fragCoord) - resolution) / resolution.y;
    vec2 warped = vec2(0.0);
    vec2 original = position;
    color = vec4(1.0, 2.0, 3.0, 0.0);
    float amplitude = 0.5;
    float time = Params.z;

    for (float index = 1.0; index < 19.0; index += 1.0) {
        amplitude += 0.03;
        time += 1.0;
        vec2 value = cos(time - 7.0 * position * pow(amplitude, index)) - 5.0 * position;
        vec4 rotation = cos(index + time * 0.02 - vec4(0.0, 11.0, 33.0, 0.0));
        position = position * mat2(rotation.x, rotation.y, rotation.z, rotation.w);
        position += 0.005 * tanh(
            40.0 * dot(position, position) * cos(100.0 * position.yx + time)
        ) + 0.2 * amplitude * position
          + 0.003 * cos(time + 4.0 * exp(-0.01 * dot(color, color)));
        warped = position / (1.0 - 2.0 * dot(position, position));
        color += (1.0 + cos(vec4(0.0, 1.0, 3.0, 0.0) + time))
                / length((1.0 + index * dot(value, value))
                * sin(warped * 3.0 - 9.0 * position.yx + time));
    }
    color = 1.0 - sqrt(exp(-color * color * color / 200.0));
    color = pow(color, vec4(0.3));
    color -= dot(original - position, original - position) / 250.0;
}

void main() {
    vec4 color;
    mainImage(color, uv * max(Params.xy, vec2(1.0)));
    finalColor = vec4(
        clamp(color.rgb * Params.w, 0.0, 1.0) * Tint.rgb,
        Tint.a
    );
}
