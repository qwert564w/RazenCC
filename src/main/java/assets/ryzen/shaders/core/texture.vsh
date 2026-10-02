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

out vec2 texCoord;
out vec4 tintColor;
out vec2 localPos;
flat out vec2 halfSize;
flat out float cornerRadius;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position.xy, 0.0, 1.0);
    texCoord = UV0;
    tintColor = Color;
    // UV1 = centered local coords, UV2 = quad size (both 1/8 px fixed-point),
    // LineWidth = corner radius in plain px.
    localPos = vec2(UV1) / UI_SIZE_SCALE;
    halfSize = vec2(UV2) / UI_SIZE_SCALE * 0.5;
    cornerRadius = LineWidth;
}
