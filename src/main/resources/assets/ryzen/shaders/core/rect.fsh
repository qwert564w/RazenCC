#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <ryzen:ui_common.glsl>
#moj_import <ryzen:ui_fragment.glsl>

in vec2 localPos;
in vec4 fillColor;
flat in vec2 halfSize;
flat in vec4 cornerRadii;
flat in vec4 effectColor;
flat in float effectValue;
flat in float shadowMode;

out vec4 fragColor;

void main() {
    vec4 effect = effectColor * ColorModulator;
    vec2 half_ = max(halfSize, vec2(0.5));
    vec4 radii = ui_fitRadii(cornerRadii, half_ * 2.0);
    float dist = ui_roundedBoxSdf(localPos, half_, radii);

    if (shadowMode > 0.5) {
        // Gaussian falloff over the SDF of the original rect: solid inside,
        // smooth bell within effectValue px outside.
        float shadowAlpha = effect.a * ui_gaussFalloff(dist, max(effectValue, 0.5));
        if (shadowAlpha <= 0.002) {
            discard;
        }
        fragColor = vec4(effect.rgb, shadowAlpha);
        return;
    }

    // Both coverages evaluate before any discard so fwidth derivatives stay
    // defined across the whole quad.
    float outerCoverage = ui_coverage(dist);
    // The border interior is the SDF offset by the thickness; for a proper
    // SDF this is the exact inset shape (corner radius shrinks by thickness).
    float borderThickness = clamp(effectValue, 0.0, min(half_.x, half_.y));
    float innerCoverage = borderThickness > 0.0 ? ui_coverage(dist + borderThickness) : outerCoverage;
    if (outerCoverage <= 0.0) {
        discard;
    }

    vec4 fill = fillColor * ColorModulator;
    float fillAlpha = fill.a * innerCoverage;
    float borderAlpha = effect.a * max(outerCoverage - innerCoverage, 0.0);

    vec4 composed = ui_compositePremul(
        vec4(effect.rgb * borderAlpha, borderAlpha),
        vec4(fill.rgb * fillAlpha, fillAlpha)
    );
    if (composed.a <= 0.0) {
        discard;
    }
    fragColor = ui_unpremultiply(composed);
}
