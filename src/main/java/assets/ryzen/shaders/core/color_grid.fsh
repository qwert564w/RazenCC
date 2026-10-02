#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <ryzen:ui_common.glsl>
#moj_import <ryzen:ui_fragment.glsl>

uniform sampler2D Sampler0;

in vec2 localPos;
in vec4 tintColor;
flat in vec2 rectSize;
flat in float cornerRadius;
flat in vec2 gridSize;
flat in vec4 highlightData;

out vec4 fragColor;

// Ring inside a cell: 1 within `thickness` of the cell edge, 0 in the middle.
float cellRing(vec2 cellPos, vec2 cellSize, float thickness) {
    vec2 distanceToEdge = min(cellPos, cellSize - cellPos);
    float edgeDistance = min(distanceToEdge.x, distanceToEdge.y);
    return 1.0 - ui_coverage(thickness - edgeDistance);
}

// 1 when this fragment's cell is the highlighted one and the ring is enabled.
float cellMatch(int cellIndex, float targetIndex, float thickness) {
    float match = (targetIndex >= -0.5 && cellIndex == int(targetIndex + 0.5)) ? 1.0 : 0.0;
    return match * step(0.001, thickness);
}

void main() {
    vec2 size = max(rectSize, vec2(1.0));
    float radius = clamp(cornerRadius, 0.0, min(size.x, size.y) * 0.5);
    vec2 centeredPos = localPos - size * 0.5;
    float dist = ui_roundedBoxSdfUniform(centeredPos, size * 0.5, radius);
    float mask = ui_coverage(dist);

    vec2 grid = max(gridSize, vec2(1.0));
    vec2 normalized = clamp(localPos / size, vec2(0.0), vec2(0.999999));
    ivec2 cell = ivec2(floor(normalized * grid));
    vec2 cellSize = size / grid;
    vec2 cellPos = localPos - vec2(cell) * cellSize;
    int cellIndex = cell.y * int(grid.x) + cell.x;

    vec2 sampleUv = (vec2(cell) + vec2(0.5)) / grid;
    vec4 color = texture(Sampler0, sampleUv) * tintColor * ColorModulator;
    color.a *= mask;

    // All derivative-based terms run in uniform control flow (no branching
    // on the per-fragment cell index); selection applies arithmetically.
    float selectedRing = max(cellRing(cellPos, cellSize, highlightData.y), 1.0 - ui_coverage(dist + highlightData.y));
    float hoveredRing = max(cellRing(cellPos, cellSize, highlightData.w), 1.0 - ui_coverage(dist + highlightData.w));
    float selected = selectedRing * cellMatch(cellIndex, highlightData.x, highlightData.y);
    float hovered = hoveredRing * cellMatch(cellIndex, highlightData.z, highlightData.w);
    float highlight = mask * max(selected, hovered) * tintColor.a * ColorModulator.a;

    color.rgb = mix(color.rgb, vec3(1.0), highlight);
    color.a = max(color.a, highlight);
    if (color.a <= 0.001) {
        discard;
    }
    fragColor = color;
}
