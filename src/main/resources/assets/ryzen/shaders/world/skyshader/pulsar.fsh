#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D DepthSampler;

#moj_import <ryzen:skyshader_common.glsl>

float pulsarHash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

// Concentric shock rings and two beams around a bright core, all falling off
// exponentially with radius so the horizon stays dark.
void main() {
    if (!isSkyDepth(texture(DepthSampler, uv).r)) {
        discard;
    }
    vec3 direction = skyDirection(uv);
    vec2 p = skyPlane(direction) * max(SkyParams.w, 0.2);
    float t = SkyParams.x * SkyParams.y * 0.3;

    float radius = length(p);
    float angle = atan(p.y, p.x);
    float pulse = 0.78 + 0.22 * sin(t * 2.0);
    float rings = pow(max(0.0, 0.5 + 0.5 * sin(radius * 45.0 - t * 5.0)), 9.0);
    rings *= exp(-radius * 2.4);
    float beams = pow(abs(cos(angle * 2.0 + t * 0.2)), 28.0) * exp(-radius * 1.6);
    float core = 0.018 / max(radius * radius, 0.004);

    vec2 grid = floor((p + vec2(t * 0.01, 0.0)) * 150.0);
    float stars = step(0.992, pulsarHash(grid)) * (0.5 + 0.5 * sin(t + pulsarHash(grid) * 20.0));

    vec3 color = BackgroundColor.rgb;
    color += mix(PrimaryColor.rgb, SecondaryColor.rgb, 0.5 + 0.5 * sin(angle + t * 0.25))
            * (rings + beams * 0.6) * pulse;
    color += mix(SecondaryColor.rgb, vec3(1.0), 0.65) * core * pulse;
    color += stars * mix(PrimaryColor.rgb, vec3(1.0), 0.7) * 0.65;
    finalColor = vec4(color, 1.0);
}
