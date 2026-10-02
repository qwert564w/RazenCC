#version 330 core

in vec2 uv;
out vec4 finalColor;

layout(std140) uniform BlockOutlineStyle {
    vec4 Tint;
    vec4 Params;
};

const float TAU = 6.28318530718;
const int ITERATIONS = 5;

void main() {
    vec2 resolution = max(Params.xy, vec2(1.0));
    float time = Params.z * 0.5 + 23.0;
    vec2 position = mod(uv * TAU, TAU) - 250.0;
    vec2 warped = position;
    float field = 1.0;
    float lineIntensity = 0.005;
    for (int index = 0; index < ITERATIONS; index++) {
        float iterationTime = time * (1.0 - 3.5 / float(index + 1));
        warped = position + vec2(
            cos(iterationTime - warped.x) + sin(iterationTime + warped.y),
            sin(iterationTime - warped.y) + cos(iterationTime + warped.x)
        );
        field += 1.0 / length(vec2(
            position.x / (sin(warped.x + iterationTime) / lineIntensity),
            position.y / (cos(warped.y + iterationTime) / lineIntensity)
        ));
    }
    field /= float(ITERATIONS);
    field = 1.17 - pow(field, 1.4);
    vec3 color = clamp(vec3(pow(abs(field), 8.0)) + vec3(0.0, 0.35, 0.5), 0.0, 1.0);
    finalColor = vec4(
        clamp(color * Params.w, 0.0, 1.0) * Tint.rgb,
        Tint.a
    );
}
