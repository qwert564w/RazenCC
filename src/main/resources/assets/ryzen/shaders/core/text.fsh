#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <ryzen:ui_common.glsl>
#moj_import <ryzen:ui_fragment.glsl>

uniform sampler2D Sampler0;

in vec2 texCoord;
in vec4 fillColor;
flat in vec4 outlineColor;
flat in float pxRange;
flat in float outlineSdf;
flat in float weightSdf;

out vec4 fragColor;

void main() {
    vec4 atlas = texture(Sampler0, texCoord);
    float screenPxRange = ui_screenPxRange(texCoord, vec2(textureSize(Sampler0, 0)), max(pxRange, 1.0));

    // MTSDF: rgb medians keep corners sharp, alpha is a true SDF without
    // corner spikes. Tiny glyphs (low px range) fade toward the true SDF,
    // which reads much cleaner than aliased msdf corners.
    float sdCrisp = ui_median3(atlas.rgb) - 0.5;
    float sdRound = atlas.a - 0.5;
    float sd = mix(sdRound, sdCrisp, clamp(screenPxRange - 1.0, 0.0, 1.0));

    // MSDF error correction: the three channels can collide inside thin
    // strokes (the bar and bowl of 'e', dots on 'i'), dipping the median
    // below the surface and punching a hole - invisible on small text but
    // sharp-edged on large text. The true SDF never collides, so where it
    // reports solidly interior we lift the median to it. Real corners sit
    // at the edge (true SDF near zero) and the bias keeps them untouched,
    // so this only ever fills holes, never rounds corners.
    sd = max(sd, sdRound - UI_MSDF_HOLE_BIAS);

    float fillAlpha = clamp((sd + weightSdf) * screenPxRange + 0.5, 0.0, 1.0);

    vec4 outline = outlineColor * ColorModulator;
    float outlineAlpha = 0.0;
    if (outlineSdf > 0.0 && outline.a > 0.0) {
        float expandedAlpha = clamp((sd + weightSdf + outlineSdf) * screenPxRange + 0.5, 0.0, 1.0);
        outlineAlpha = outline.a * max(expandedAlpha - fillAlpha, 0.0);
    }

    vec4 fill = fillColor * ColorModulator;
    vec4 composed = ui_compositePremul(
        vec4(outline.rgb * outlineAlpha, outlineAlpha),
        vec4(fill.rgb * (fill.a * fillAlpha), fill.a * fillAlpha)
    );
    if (composed.a <= 0.001) {
        discard;
    }
    fragColor = ui_unpremultiply(composed);
}
