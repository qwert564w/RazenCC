// Blade 2D UI shared library (stage-agnostic: safe for .vsh and .fsh).
//
// Vertex attribute packing contract (mirrors org.blade.utils.render.gui.UiVertexPacking):
//   - "pair" i16: two unsigned bytes packed into one 16-bit int attribute
//   - "z-packed" float: mode(1b) | units12(value * 16) | alpha8, exact below 2^24
//   - "dual12" float: hi12 | lo12 fixed-point values, exact below 2^24
//   - sizes travel as 1/8 px fixed-point, radii/thickness as 1/16 px fixed-point

const float UI_SIZE_SCALE = 8.0;     // 1/8 px fixed-point for sizes
const float UI_RADIUS_SCALE = 16.0;  // 1/16 px fixed-point for radii and thickness

// How deep inside a stroke the true SDF must be before it overrides a
// collided MSDF median (see text.fsh). Small enough to leave edges and
// corners untouched, large enough to swallow thin-feature holes.
const float UI_MSDF_HOLE_BIAS = 0.0625;

float ui_median3(vec3 v) {
    return max(min(v.r, v.g), min(max(v.r, v.g), v.b));
}

// Two u8 values from one 16-bit int attribute, normalized to 0..1.
vec2 ui_unpackU8Pair(int encoded) {
    return vec2(float(encoded & 255), float((encoded >> 8) & 255)) / 255.0;
}

// RGBA color from two 16-bit int attributes (rg | ba).
vec4 ui_unpackColor(int packedRg, int packedBa) {
    return vec4(ui_unpackU8Pair(packedRg), ui_unpackU8Pair(packedBa));
}

// z-packed float: returns vec3(mode, value px, alpha 0..1).
// Layout: (mode << 12 | units12) * 256 + alpha8 -> max 2^21, exact in f32.
vec3 ui_unpackZ(float encoded) {
    float units = floor(encoded / 256.0);
    float alpha = encoded - units * 256.0;
    float mode = step(4096.0, units);
    units -= mode * 4096.0;
    return vec3(mode, units / UI_RADIUS_SCALE, alpha / 255.0);
}

// dual12 float: two 12-bit raw integer values.
vec2 ui_unpackDual12Raw(float encoded) {
    float hi = floor(encoded / 4096.0);
    float lo = encoded - hi * 4096.0;
    return vec2(hi, lo);
}

// dual12 float: two 12-bit fixed-point values, returned in px.
vec2 ui_unpackDual12(float encoded) {
    return ui_unpackDual12Raw(encoded) / UI_RADIUS_SCALE;
}

// Corner radius for the quadrant of a centered point: x = TL, y = TR, z = BR, w = BL.
float ui_selectRadius(vec2 point, vec4 radii) {
    if (point.x >= 0.0) {
        return point.y < 0.0 ? radii.y : radii.z;
    }
    return point.y < 0.0 ? radii.x : radii.w;
}

// Scales radii down so opposing corners never overlap (CSS border-radius rules).
vec4 ui_fitRadii(vec4 radii, vec2 size) {
    radii = max(radii, 0.0);
    float epsilon = 0.0001;
    float scale = min(1.0, min(
        min(size.x / max(radii.x + radii.y, epsilon), size.x / max(radii.w + radii.z, epsilon)),
        min(size.y / max(radii.x + radii.w, epsilon), size.y / max(radii.y + radii.z, epsilon))
    ));
    return radii * scale;
}

// Signed distance to a rounded box with per-corner radii; point is centered.
float ui_roundedBoxSdf(vec2 point, vec2 halfSize, vec4 radii) {
    float radius = ui_selectRadius(point, radii);
    vec2 q = abs(point) - halfSize + vec2(radius);
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

// Uniform-radius variant (cheaper: no quadrant select).
float ui_roundedBoxSdfUniform(vec2 point, vec2 halfSize, float radius) {
    vec2 q = abs(point) - halfSize + vec2(radius);
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

// Gaussian-like falloff over an SDF distance: 1 inside, smooth bell outside,
// ~0.01 at dist == radius. Much softer than a linear smoothstep shadow.
float ui_gaussFalloff(float dist, float radius) {
    float x = max(dist, 0.0) / max(radius, 0.0001);
    return exp(-4.5 * x * x);
}

vec4 ui_premultiply(vec4 color) {
    return vec4(color.rgb * color.a, color.a);
}

// Source-over composite of two premultiplied colors (front over back).
vec4 ui_compositePremul(vec4 back, vec4 front) {
    return front + back * (1.0 - front.a);
}

// Premultiplied -> straight alpha, guarding the divide.
vec4 ui_unpremultiply(vec4 premul) {
    return vec4(premul.rgb / max(premul.a, 0.0001), premul.a);
}

// Small screen-space hash dither (+-0.5/255) to hide 8-bit gradient banding.
float ui_dither(vec2 fragPos) {
    return (fract(sin(dot(fragPos, vec2(12.9898, 78.233))) * 43758.5453) - 0.5) / 255.0;
}
