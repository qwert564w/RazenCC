#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <ryzen:ui_common.glsl>
#moj_import <ryzen:ui_fragment.glsl>

uniform sampler2D Sampler0;

in vec2 texCoord;
in vec4 tintColor;
in vec2 localPos;
flat in vec2 halfSize;
flat in float cornerRadius;

out vec4 fragColor;

void main() {
    vec4 sampled = texture(Sampler0, texCoord) * tintColor * ColorModulator;

    if (cornerRadius > 0.0 && halfSize.x > 0.0 && halfSize.y > 0.0) {
        float radius = min(cornerRadius, min(halfSize.x, halfSize.y));
        float dist = ui_roundedBoxSdfUniform(localPos, halfSize, radius);
        // Fade alpha only: scaling rgb as well would darken the AA rim.
        sampled.a *= ui_coverage(dist);
    }

    if (sampled.a <= 0.001) {
        discard;
    }
    fragColor = sampled;
}
