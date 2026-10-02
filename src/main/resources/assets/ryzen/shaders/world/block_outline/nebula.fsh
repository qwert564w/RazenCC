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
    vec3 position = direction * 2.2;
    vec3 warp = vec3(
        fbm(position * 0.7 + vec3(time * 0.02, 0.0, 0.0)),
        fbm(position * 0.7 + vec3(5.2, time * 0.018, 1.3)),
        fbm(position * 0.7 + vec3(1.7, 9.2, -time * 0.015))
    );
    position += (warp - 0.5) * 2.0;
    float firstNoise = fbm(position * 1.1);
    float secondNoise = fbm(position * 2.4 + 4.0);
    float density = pow(
        smoothstep(0.15, 0.85, firstNoise * 0.7 + secondNoise * 0.3),
        1.15
    );
    float hue = fbm(position * 0.6 + 9.0);
    vec3 primary = vec3(0.227, 0.420, 1.0);
    vec3 secondary = vec3(0.753, 0.380, 1.0);
    vec3 nebula = mix(primary, secondary, smoothstep(0.18, 0.85, hue));
    nebula = mix(nebula, nebula * 1.8 + 0.30, density);
    vec3 color = vec3(0.016, 0.020, 0.038);
    color += nebula * density * 1.85;
    color += mix(primary, secondary, 0.5) * pow(firstNoise, 2.0) * 0.22;
    color += vec3(0.85, 0.90, 1.0) * starGlow(direction, 200.0, 0.08, time);
    color += vec3(1.0, 0.96, 0.90)
            * starGlow(direction * 1.7 + 31.0, 110.0, 0.045, time * 1.3)
            * 1.4;
    color *= Params.w;
    color = color / (1.0 + color);
    color = pow(color, vec3(0.82)) + ditherRgb(gl_FragCoord.xy);
    finalColor = vec4(clamp(color, 0.0, 1.0) * Tint.rgb, Tint.a);
}
