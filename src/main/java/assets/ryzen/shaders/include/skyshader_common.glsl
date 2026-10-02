// Shared by the SkyShader fullscreen passes.

layout(std140) uniform SkyShaderUniforms {
    mat4 InvViewProjection;
    vec4 PrimaryColor;    // rgb = first gradient stop
    vec4 SecondaryColor;  // rgb = second gradient stop
    vec4 BackgroundColor; // rgb = deep background
    vec4 VanillaSky;      // rgb = the sky colour this pass replaces
    vec4 SkyParams;       // x = time, y = speed, z = intensity, w = scale
    vec4 SkyParams2;      // x = overlay, y = variant, z = reversed 0..1 depth
};

vec3 skyDirection(vec2 coords) {
    vec2 ndc = coords * 2.0 - 1.0;
    float nearZ = SkyParams2.z > 0.5 ? 1.0 : -1.0;
    float farZ = SkyParams2.z > 0.5 ? 0.0 : 1.0;
    vec4 nearPoint = InvViewProjection * vec4(ndc, nearZ, 1.0);
    vec4 farPoint = InvViewProjection * vec4(ndc, farZ, 1.0);
    return normalize(
        farPoint.xyz / farPoint.w - nearPoint.xyz / nearPoint.w
    );
}

bool isSkyDepth(float depth) {
    return SkyParams2.z > 0.5
        ? depth <= 0.000001
        : depth >= 0.999999;
}

// Flattens a view ray onto a plane so the pattern stays readable near the
// horizon instead of stretching to infinity.
vec2 skyPlane(vec3 direction) {
    return direction.xz / max(abs(direction.y) + 0.35, 0.35);
}

float skyHash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float skyNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(skyHash(i), skyHash(i + vec2(1.0, 0.0)), f.x),
        mix(skyHash(i + vec2(0.0, 1.0)), skyHash(i + 1.0), f.x),
        f.y
    );
}

float skyFbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 5; i++) {
        value += skyNoise(p) * amplitude;
        p = mat2(1.6, 1.2, -1.2, 1.6) * p;
        amplitude *= 0.5;
    }
    return value;
}
