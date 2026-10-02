#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <ryzen:ui_common.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;

out vec2 localPos;
out vec2 screenUv;
out vec4 tintColor;
flat out vec2 halfSize;
flat out float cornerRadius;
flat out float overallAlpha;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position.xy, 0.0, 1.0);
    // Backdrop UV straight from clip space: works identically on OpenGL and
    // Vulkan backends (no gl_FragCoord origin assumptions).
    screenUv = gl_Position.xy / gl_Position.w * 0.5 + 0.5;
    localPos = UV0;
    tintColor = Color;
    halfSize = vec2(UV1) / UI_SIZE_SCALE * 0.5;
    cornerRadius = float(UV2.x) / UI_RADIUS_SCALE;
    overallAlpha = float(UV2.y & 255) / 255.0;
}
