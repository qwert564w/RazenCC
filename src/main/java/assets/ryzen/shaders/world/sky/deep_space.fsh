#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D DepthSampler;
uniform sampler2D CloudSampler;

#moj_import <ryzen:block_outline_common.glsl>
#moj_import <ryzen:world_sky_common.glsl>

// Full-res composite: bilinear-upsampled smooth layer plus exact stars.
void main() {
    if (!isSkyDepth(texture(DepthSampler, uv).r)) {
        discard;
    }
    vec3 direction = skyDirection(uv);
    float time = SkyParams.x * SkyParams.z;
    vec4 cloud = texture(CloudSampler, uv);
    float stars = starGlow(direction, 300.0, 0.06, time)
            + starGlow(direction * 1.8 + 5.0, 160.0, 0.03, time * 1.3) * 1.3;
    vec3 color = cloud.rgb + vec3(0.90, 0.93, 1.0) * stars * (0.7 + 0.9 * cloud.a);
    color *= SkyParams.y;
    color = color / (1.0 + color * 0.8);
    color = pow(color, vec3(0.88)) + ditherRgb(gl_FragCoord.xy);
    finalColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
