#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D SceneSampler;

layout(std140) uniform JumpWaveUniforms {
    // x = ring progress (0..1), y = strength, z = fade (0..1), w = unused
    vec4 params;
    vec4 crestColor;   // rgb = glow/jump color, a = color alpha
};

#define RING_WIDTH 0.30

void main() {
    float ringProgress = params.x;
    float strength = params.y;
    float fade = params.z;

    // Centered quad coords in [-1, 1], distance from the jump point.
    vec2 centered = (uv - 0.5) * 2.0;
    float dist = length(centered);
    if (dist > 1.0) {
        discard;
    }

    // Expanding ring: a soft band located at the current progress radius (the crest).
    // Smooth on both sides so the leading edge of the distortion is gradual, not hard.
    float ringRadius = ringProgress;
    float edge = dist - ringRadius;
    float band = smoothstep(RING_WIDTH, 0.0, abs(edge));
    band *= band; // sharper core, softer tails

    // Base glass fill across the whole quad, alive for the wave's entire lifetime so
    // the jump circle underneath is ALWAYS refracted, not only once the ring sweeps
    // past it. Soft falloff at the quad edge so it blends out instead of cutting off.
    float inside = smoothstep(1.0, 0.82, dist);

    // Total liquid-glass coverage: the whole disc plus the bright moving crest.
    float coverage = max(inside, band);
    if (coverage <= 0.001) {
        discard;
    }

    // Radial direction of the distortion in screen space.
    vec2 dir = dist > 0.0001 ? centered / dist : vec2(0.0);

    vec2 screenSize = vec2(textureSize(SceneSampler, 0));
    vec2 screenUv = gl_FragCoord.xy / screenSize;

    // A little ripple riding along the crest gives the glass a moving, liquid feel.
    float ripple = sin(dist * 38.0 - ringProgress * 26.0) * 0.5 + 0.5;
    float crest = band * mix(0.7, 1.3, ripple);

    // Distortion across the whole disc (so the circle is always bent), with a strong
    // extra push right on the moving crest so the leading edge bends hardest.
    float innerDistort = inside * mix(0.55, 1.0, dist);
    float offsetAmount = strength * fade * 0.05 * (innerDistort + crest * 1.6);

    // Single-tap refraction: bend the whole sample uniformly so it reads as clear
    // glass, not an oily rainbow. No per-channel split -> no chromatic fringing.
    vec2 baseOffset = dir * offsetAmount;
    vec3 refracted = texture(SceneSampler, clamp(screenUv + baseOffset, vec2(0.0), vec2(1.0))).rgb;

    // Fat glassy rim on the leading crest, tinted slightly with the glow/jump color so
    // the bortik reads clearly. White-ish specular core + a colored bleed around it.
    float crestHighlight = pow(band, 1.5) * fade * 0.5;
    float sheen = inside * fade * 0.06;
    vec3 crestTint = crestColor.rgb * pow(band, 1.2) * fade * 0.55;
    vec3 color = refracted + vec3(crestHighlight + sheen) + crestTint;

    // The disc must fully replace the scene, otherwise the original jump circle drawn
    // underneath shows through undistorted. inside is opaque across the whole disc and
    // only softens at the quad edge, so the refracted (circle-included) copy wins.
    float alpha = max(inside, band) * fade;
    finalColor = vec4(color, alpha);
}
