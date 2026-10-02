#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <ryzen:ui_common.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;
in float LineWidth;

out vec2 texCoord;
out vec4 fillColor;
flat out vec4 outlineColor;
flat out float pxRange;
flat out float outlineSdf;
flat out float weightSdf;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position.xy, 0.0, 1.0);
    texCoord = UV0;
    fillColor = Color;
    outlineColor = ui_unpackColor(UV1.x, UV1.y);
    pxRange = float(UV2.x) / UI_RADIUS_SCALE;

    // Position.z = local px per SDF unit; converts px-domain effects
    // (outline thickness, synthetic weight) into distance-field units so
    // they stay correct under any pose/gui scaling.
    float localPxPerSdfUnit = max(Position.z, 0.0001);
    outlineSdf = LineWidth / localPxPerSdfUnit;
    weightSdf = Normal.x / localPxPerSdfUnit;
}
