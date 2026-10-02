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

out vec2 localPos;
out vec4 fillColor;
flat out vec2 halfSize;
flat out vec4 cornerRadii;
flat out vec4 effectColor;
flat out float effectValue;
flat out float shadowMode;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position.xy, 0.0, 1.0);

    // UV0 is centered on the rect: corners sit at +-(halfSize + shadow spread).
    localPos = UV0;
    fillColor = Color;
    halfSize = vec2(UV1) / UI_SIZE_SCALE * 0.5;

    // Radii: TL/TR ride full 15-bit shorts, BR/BL share one dual12 float.
    vec2 bottomRadii = ui_unpackDual12(LineWidth);
    cornerRadii = vec4(float(UV2.x) / UI_RADIUS_SCALE, float(UV2.y) / UI_RADIUS_SCALE, bottomRadii.x, bottomRadii.y);

    // Position.z carries mode | border-or-blur value | effect alpha.
    vec3 packedZ = ui_unpackZ(Position.z);
    shadowMode = packedZ.x;
    effectValue = packedZ.y;
    effectColor = vec4(Normal * 0.5 + 0.5, packedZ.z);
}
