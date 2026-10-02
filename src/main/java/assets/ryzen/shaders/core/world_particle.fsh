#version 330 core

in vec2 uv;
in vec4 tint;
out vec4 finalColor;

uniform sampler2D BloomSampler;

layout(std140) uniform WorldParticleUniforms {
    vec4 params; // x = brightness multiplier
};

void main() {
    // Bloom texture is grayscale (hot core + halo + faint star streaks).
    float intensity = texture(BloomSampler, uv).r;
    float strength = intensity * tint.a;
    if (strength <= 0.004) {
        discard;
    }

    // Colored glow that burns to white at the core, like an emissive light
    // through a bloom pass. Rendered with additive (ONE, ONE) blending, so the
    // fragment color is the amount of light added to the scene; brightness
    // above 1 overdrives it into the RTX-style bloom look.
    vec3 glow = tint.rgb * strength;
    glow = mix(glow, vec3(strength), pow(intensity, 4.0) * 0.85);
    finalColor = vec4(glow * params.x, strength);
}
