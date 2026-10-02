#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D DepthSampler;

#moj_import <ryzen:block_outline_common.glsl>
#moj_import <ryzen:world_sky_common.glsl>

// Half-res pass: all six fbm evaluations of the nebula live here.
void main() {
    if (!skyNearby(DepthSampler, uv)) {
        finalColor = vec4(0.0);
        return;
    }
    vec3 direction = skyDirection(uv);
    float time = SkyParams.x * SkyParams.z;
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
        smoothstep(0.32, 0.95, firstNoise * 0.7 + secondNoise * 0.3),
        1.4
    );
    float hue = fbm(position * 0.6 + 9.0);
    vec3 nebula = mix(
        PrimaryColor.rgb,
        SecondaryColor.rgb,
        smoothstep(0.18, 0.85, hue)
    );
    nebula = mix(nebula, nebula * 1.7 + 0.25, density);
    vec3 cloud = vec3(0.010, 0.013, 0.024);
    cloud += nebula * density * 1.35;
    cloud += mix(PrimaryColor.rgb, SecondaryColor.rgb, 0.5)
            * pow(firstNoise, 2.0) * 0.12;
    finalColor = vec4(cloud, 0.0);
}
