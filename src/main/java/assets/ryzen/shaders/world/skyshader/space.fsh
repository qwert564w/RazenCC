#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D DepthSampler;

#moj_import <ryzen:skyshader_common.glsl>

// Nebula clouds plus a twinkling star field, blended over the vanilla sky by
// the overlay amount so a resource pack's own sky can still show through.
void main() {
    if (!isSkyDepth(texture(DepthSampler, uv).r)) {
        discard;
    }
    vec3 direction = skyDirection(uv);
    vec2 plane = skyPlane(direction);
    float time = SkyParams.x;
    float intensity = SkyParams.z;
    float t = time * SkyParams.y * 0.025;

    float cloud = skyFbm(plane * 2.8 + vec2(t, -t * 0.65));
    float detail = skyFbm(plane * 6.0 - vec2(t * 0.4, t * 0.25));
    float nebula = smoothstep(0.28, 0.92, cloud * 0.82 + detail * 0.35);
    vec3 color = mix(
        BackgroundColor.rgb,
        mix(PrimaryColor.rgb, SecondaryColor.rgb, detail),
        nebula * intensity
    );

    vec2 starGrid = plane * 165.0;
    vec2 cell = floor(starGrid);
    vec2 point = fract(starGrid) - 0.5;
    float seed = skyHash(cell);
    vec2 jitter = (vec2(skyHash(cell + 3.1), skyHash(cell + 7.7)) - 0.5) * 0.7;
    float star = smoothstep(0.065, 0.0, length(point - jitter));
    star *= smoothstep(0.965, 1.0, seed);
    star *= 0.65 + 0.35 * sin(time * (1.0 + seed * 2.0) + seed * 30.0);
    color += star * mix(vec3(0.8, 0.9, 1.0), SecondaryColor.rgb, seed) * (1.4 * intensity);

    vec3 vanilla = mix(
        VanillaSky.rgb * 0.35,
        VanillaSky.rgb,
        smoothstep(-0.25, 0.85, direction.y)
    );
    finalColor = vec4(mix(vanilla, color, SkyParams2.x), 1.0);
}
