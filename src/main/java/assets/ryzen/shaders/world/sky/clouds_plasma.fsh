#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D DepthSampler;

#moj_import <ryzen:block_outline_common.glsl>
#moj_import <ryzen:world_sky_common.glsl>

// Half-res pass: the four chained fbm evaluations of the plasma sky.
void main() {
    if (!skyNearby(DepthSampler, uv)) {
        finalColor = vec4(0.0);
        return;
    }
    vec3 direction = skyDirection(uv);
    float time = SkyParams.x * SkyParams.z;
    vec3 position = direction * 4.2;
    float firstWarp = fbm(
        position * 0.65 + vec3(time * 0.055, -time * 0.07, time * 0.035)
    );
    float secondWarp = fbm(
        position * 1.3
        + vec3(firstWarp * 2.2)
        + vec3(-time * 0.045, time * 0.06, -time * 0.03)
    );
    float value = fbm(
        position * 1.8
        + vec3(secondWarp * 2.4)
        + vec3(time * 0.025, -time * 0.065, time * 0.04)
    );
    float energy = smoothstep(0.15, 0.90, value);
    float vein = pow(energy, 2.5);
    float hue = fbm(position * 0.85 + vec3(secondWarp + time * 0.012));
    vec3 plasmaColor = mix(
        PrimaryColor.rgb,
        SecondaryColor.rgb,
        smoothstep(0.2, 0.85, hue)
    );
    vec3 cloud = mix(plasmaColor * 0.18, plasmaColor * 1.7, energy);
    cloud += plasmaColor * vein * 0.9;
    finalColor = vec4(cloud, 0.0);
}
