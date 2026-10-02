#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D DepthSampler;

#moj_import <ryzen:skyshader_common.glsl>

float summerClouds(vec2 p) {
    float value = skyNoise(p) * 0.58;
    value += skyNoise(p * 2.03) * 0.28;
    value += skyNoise(p * 4.07) * 0.14;
    return value;
}

// Day/night is one uniform (Variant) rather than two shaders: every colour
// pair below is mixed by it, and the stars are simply multiplied out by day.
void main() {
    if (!isSkyDepth(texture(DepthSampler, uv).r)) {
        discard;
    }
    vec3 direction = skyDirection(uv);
    vec2 plane = skyPlane(direction);
    float variant = SkyParams2.y;
    float t = SkyParams.x * SkyParams.y * 0.012;

    float cloud = smoothstep(0.52, 0.78, summerClouds(plane * (2.7 * SkyParams.w) + vec2(t, 0.0)));

    vec3 dayBottom = mix(vec3(0.52, 0.78, 1.0), PrimaryColor.rgb, 0.35);
    vec3 dayTop = mix(vec3(0.08, 0.36, 0.78), PrimaryColor.rgb, 0.2);
    vec3 nightBottom = vec3(0.08, 0.12, 0.28);
    vec3 nightTop = vec3(0.005, 0.012, 0.055);

    float vertical = smoothstep(-0.25, 0.85, direction.y);
    vec3 sky = mix(
        mix(dayBottom, dayTop, vertical),
        mix(nightBottom, nightTop, vertical),
        variant
    );
    vec3 cloudColor = mix(mix(vec3(1.0), SecondaryColor.rgb, 0.22), vec3(0.2, 0.25, 0.48), variant);

    float stars = step(0.993, skyHash(floor(plane * 175.0))) * variant;
    sky = mix(sky, cloudColor, cloud * mix(0.72, 0.35, variant));
    sky += stars * vec3(0.75, 0.85, 1.0);
    finalColor = vec4(sky, 1.0);
}
