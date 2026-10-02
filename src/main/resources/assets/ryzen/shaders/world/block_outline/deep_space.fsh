#version 330 core

in vec2 uv;
out vec4 finalColor;

layout(std140) uniform BlockOutlineStyle {
    vec4 Tint;
    vec4 Params;
};

#moj_import <ryzen:block_outline_common.glsl>

vec3 decodeDirection(vec2 encoded) {
    vec2 value = encoded * 2.0 - 1.0;
    value.y = -value.y;
    vec3 direction = vec3(
        value.x,
        value.y,
        1.0 - abs(value.x) - abs(value.y)
    );
    float correction = clamp(-direction.z, 0.0, 1.0);
    direction.x += direction.x >= 0.0 ? -correction : correction;
    direction.y += direction.y >= 0.0 ? -correction : correction;
    return normalize(direction);
}

void main() {
    vec3 direction = decodeDirection(uv);
    float time = Params.z;
    vec3 color = vec3(0.014, 0.018, 0.034);
    vec3 bandNormal = normalize(vec3(0.30, 0.62, 0.72));
    float band = exp(-pow(dot(direction, bandNormal) * 1.25, 2.0));
    float haze = fbm(direction * 3.0 + vec3(time * 0.01, 0.0, 0.0));
    color += mix(vec3(0.227, 0.420, 1.0), vec3(0.753, 0.380, 1.0), 0.5)
            * (0.10 + band * (0.22 + 0.40 * haze));
    float stars = starGlow(direction, 220.0, 0.11, time)
            + starGlow(direction * 1.8 + 5.0, 120.0, 0.06, time * 1.3) * 1.3;
    color += vec3(0.90, 0.93, 1.0) * stars * (0.8 + 0.7 * band);
    color *= Params.w;
    color = color / (1.0 + color * 0.8);
    color = pow(color, vec3(0.85)) + ditherRgb(gl_FragCoord.xy);
    finalColor = vec4(clamp(color, 0.0, 1.0) * Tint.rgb, Tint.a);
}
