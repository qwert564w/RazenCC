#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D DepthSampler;

#moj_import <ryzen:skyshader_common.glsl>

// Four interfering sine waves; the fourth is radial, which keeps the pattern
// from reading as a flat grid when looking straight up.
void main() {
    if (!isSkyDepth(texture(DepthSampler, uv).r)) {
        discard;
    }
    vec3 direction = skyDirection(uv);
    vec2 p = skyPlane(direction) * (3.2 * SkyParams.w);
    float t = SkyParams.x * SkyParams.y * 0.35;

    float wave = sin(p.x * 2.1 + t);
    wave += sin(p.y * 2.7 - t * 1.3);
    wave += sin((p.x + p.y) * 1.8 + t * 0.7);
    wave += sin(length(p + vec2(sin(t * 0.2), cos(t * 0.17))) * 4.0 - t);

    float energy = 0.5 + 0.125 * wave;
    vec3 color = mix(PrimaryColor.rgb, SecondaryColor.rgb, smoothstep(0.1, 0.9, energy));
    color = mix(BackgroundColor.rgb, color, 0.35 + energy * 0.75);
    color += pow(max(energy, 0.0), 4.0) * mix(SecondaryColor.rgb, vec3(1.0), 0.45) * 0.45;
    finalColor = vec4(color, 1.0);
}
