// Blade 2D UI fragment-only helpers (fwidth is not available in vertex shaders).

// Coverage of a signed distance with exactly one device pixel of smoothing,
// regardless of GUI scale or pose scaling. dist < 0 is inside.
float ui_coverage(float dist) {
    float width = max(fwidth(dist), 0.0001);
    return clamp(0.5 - dist / width, 0.0, 1.0);
}

// Coverage with a minimum softness in local units (for intentionally soft edges).
float ui_coverageSoft(float dist, float softness) {
    float width = max(fwidth(dist), max(softness, 0.0001));
    return clamp(0.5 - dist / width, 0.0, 1.0);
}

// msdfgen reference screen-pixel-range: how many screen pixels one distance
// unit spans at the current sampling density.
float ui_screenPxRange(vec2 texCoord, vec2 atlasSize, float pxRange) {
    vec2 unitRange = vec2(pxRange) / atlasSize;
    vec2 screenTexSize = vec2(1.0) / max(fwidth(texCoord), vec2(0.0000001));
    return max(0.5 * dot(unitRange, screenTexSize), 1.0);
}
