#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D DepthSampler;
uniform sampler2D CloudSampler;

#moj_import <ryzen:block_outline_common.glsl>
#moj_import <ryzen:world_sky_common.glsl>

// Full-res composite: bilinear-upsampled plasma plus exact stars.
void main() {
    if (!isSkyDepth(texture(DepthSampler, uv).r)) {
        discard;
    }
    vec3 direction = skyDirection(uv);
    float time = SkyParams.x * SkyParams.z;
    vec3 color = texture(CloudSampler, uv).rgb;
    color += vec3(0.8, 0.9, 1.0)
            * starGlow(direction, 180.0, 0.025, time * 0.2)
            * 0.35;
    color *= SkyParams.y;
    color = color / (1.0 + color * 0.75);
    color = pow(color, vec3(0.88)) + ditherRgb(gl_FragCoord.xy);
    finalColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
