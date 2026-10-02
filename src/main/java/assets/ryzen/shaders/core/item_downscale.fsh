#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

// Atlas pixels per original item texel (= GUI scale). This is used only to
// keep the bicubic footprint inside the current atlas slot.
#ifndef TEXEL_SIZE
#define TEXEL_SIZE 1
#endif

float cubicWeight(float distance) {
    float x = abs(distance);
    if (x < 1.0) {
        return ((1.5 * x - 2.5) * x) * x + 1.0;
    }
    if (x < 2.0) {
        return ((-0.5 * x + 2.5) * x - 4.0) * x + 2.0;
    }
    return 0.0;
}

vec4 sampleAtlasPixel(vec2 pixel, vec2 atlasSize, vec2 slotMin, vec2 slotMax) {
    vec2 center = clamp(pixel + vec2(0.5), slotMin, slotMax);
    return texture(Sampler0, center / atlasSize);
}

// Catmull-Rom bicubic reduction on the rendered atlas pixels. Unlike the
// previous source-texel snapping, this preserves the atlas' high-resolution
// model edges and glint while smoothing uneven fractional downscaling. The
// slightly negative Catmull-Rom lobes retain more contrast than bilinear, so
// the result does not turn soft.
void main() {
    vec2 atlasSize = vec2(textureSize(Sampler0, 0));
    vec2 source = texCoord0 * atlasSize - vec2(0.5);
    vec2 base = floor(source);
    vec2 fraction = source - base;

    float slotSize = 16.0 * float(TEXEL_SIZE);
    vec2 safePixel = clamp(source, vec2(0.0), atlasSize - vec2(1.0));
    vec2 slotOrigin = floor(safePixel / slotSize) * slotSize;
    vec2 slotMin = slotOrigin + vec2(0.5);
    vec2 slotMax = slotOrigin + vec2(slotSize - 0.5);

    vec4 color = vec4(0.0);
    float totalWeight = 0.0;
    for (int y = -1; y <= 2; ++y) {
        float weightY = cubicWeight(float(y) - fraction.y);
        for (int x = -1; x <= 2; ++x) {
            float weight = cubicWeight(float(x) - fraction.x) * weightY;
            color += sampleAtlasPixel(base + vec2(x, y), atlasSize, slotMin, slotMax) * weight;
            totalWeight += weight;
        }
    }

    color = clamp(color / max(totalWeight, 1.0e-6), 0.0, 1.0) * vertexColor;
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color * ColorModulator;
}
