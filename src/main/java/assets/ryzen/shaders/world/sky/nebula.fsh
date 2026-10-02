#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D DepthSampler;
uniform sampler2D CloudSampler;

#moj_import <ryzen:block_outline_common.glsl>
#moj_import <ryzen:world_sky_common.glsl>

// Full-res composite: bilinear-upsampled nebula plus exact stars.
void main() {
    if (!isSkyDepth(texture(DepthSampler, uv).r)) {
        discard;
    }
    vec3 direction = skyDirection(uv);
    float time = SkyParams.x * SkyParams.z;
    vec3 color = texture(CloudSampler, uv).rgb;
    color += vec3(0.85, 0.90, 1.0)
            * starGlow(direction, 240.0, 0.05, time);
    color += vec3(1.0, 0.96, 0.90)
            * starGlow(direction * 1.7 + 31.0, 130.0, 0.025, time * 1.3)
            * 1.4;
    color *= SkyParams.y;
    color = color / (1.0 + color);
    color = pow(color, vec3(0.85)) + ditherRgb(gl_FragCoord.xy);
    finalColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
