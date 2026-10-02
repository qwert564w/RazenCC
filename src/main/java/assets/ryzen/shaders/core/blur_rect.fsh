#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <ryzen:ui_common.glsl>
#moj_import <ryzen:ui_fragment.glsl>

uniform sampler2D Sampler0;

in vec2 localPos;
in vec2 screenUv;
in vec4 tintColor;
flat in vec2 halfSize;
flat in float cornerRadius;
flat in float overallAlpha;

out vec4 fragColor;

void main() {
    vec2 half_ = max(halfSize, vec2(0.5));
    float radius = clamp(cornerRadius, 0.0, min(half_.x, half_.y));
    float dist = ui_roundedBoxSdfUniform(localPos, half_, radius);
    float mask = ui_coverage(dist);
    if (mask <= 0.003) {
        discard;
    }

    // Backdrop was blurred once per glass run by GuiBackdrop (dual-kawase at
    // reduced resolution); a single bilinear tap replaces the old per-pixel
    // 33x33 gaussian loop.
    vec3 blurred = texture(Sampler0, clamp(screenUv, vec2(0.0), vec2(1.0))).rgb;
    vec4 tint = tintColor * ColorModulator;
    vec3 panel = mix(blurred, tint.rgb, clamp(tint.a, 0.0, 1.0));
    panel += vec3(ui_dither(gl_FragCoord.xy));

    float alpha = mask * overallAlpha;
    if (alpha <= 0.003) {
        discard;
    }
    fragColor = vec4(panel, alpha);
}
