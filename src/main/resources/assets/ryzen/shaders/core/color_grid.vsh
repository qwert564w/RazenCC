#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <ryzen:ui_common.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in float LineWidth;

out vec2 localPos;
out vec4 tintColor;
flat out vec2 rectSize;
flat out float cornerRadius;
flat out vec2 gridSize;
flat out vec4 highlightData;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position.xy, 0.0, 1.0);
    localPos = UV0;
    tintColor = Color;
    rectSize = vec2(UV1) / UI_SIZE_SCALE;
    cornerRadius = LineWidth;
    gridSize = vec2(float(UV2.y & 255), float((UV2.y >> 8) & 255));

    // Position.z packs (index + 1) pairs, 0 = no highlight; UV2.x packs the
    // ring thicknesses at 1/8 px.
    vec2 indices = ui_unpackDual12Raw(Position.z) - 1.0;
    vec2 thickness = vec2(float(UV2.x & 255), float((UV2.x >> 8) & 255)) / UI_SIZE_SCALE;
    highlightData = vec4(indices.x, thickness.x, indices.y, thickness.y);
}
