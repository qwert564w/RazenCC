// Shared by the half-res sky cloud passes and the full-res composites.

layout(std140) uniform WorldSkyUniforms {
    mat4 InvViewProjection;
    vec4 PrimaryColor;
    vec4 SecondaryColor;
    vec4 SkyParams; // x = time, y = intensity, z = speed, w = reversed 0..1 depth
    vec4 SkyTexel;  // xy = full-res depth texel size
};

vec3 skyDirection(vec2 coords) {
    vec2 ndc = coords * 2.0 - 1.0;
    float nearZ = SkyParams.w > 0.5 ? 1.0 : -1.0;
    float farZ = SkyParams.w > 0.5 ? 0.0 : 1.0;
    vec4 nearPoint = InvViewProjection * vec4(ndc, nearZ, 1.0);
    vec4 farPoint = InvViewProjection * vec4(ndc, farZ, 1.0);
    return normalize(
        farPoint.xyz / farPoint.w - nearPoint.xyz / nearPoint.w
    );
}

bool isSkyDepth(float depth) {
    return SkyParams.w > 0.5
        ? depth <= 0.000001
        : depth >= 0.999999;
}

// True when any depth texel within one half-res texel of `coords` is sky.
// The dilation guarantees the composite's bilinear upsample never blends
// in a cloud texel that skipped shading.
bool skyNearby(sampler2D depthSampler, vec2 coords) {
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            float depth = texture(depthSampler, coords + vec2(float(x), float(y)) * SkyTexel.xy * 2.0).r;
            if (isSkyDepth(depth)) {
                return true;
            }
        }
    }
    return false;
}
