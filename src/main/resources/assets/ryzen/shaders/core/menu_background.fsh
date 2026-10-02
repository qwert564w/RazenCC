#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <ryzen:ui_common.glsl>
#moj_import <ryzen:ui_fragment.glsl>

in vec2 quadUv;
in vec2 aspect;
in vec4 primaryColor;
flat in vec4 secondaryColor;
flat in float mode;
flat in float elapsed;
flat in float cornerRadius;

out vec4 fragColor;

// Modes (mirror org.blade.menu.core.MenuBackground):
const float MODE_NEBULA = 1.0;
const float MODE_PLASMA = 2.0;
const float MODE_GYROID = 3.0;

// Signed distance to a rounded box in aspect-corrected uv space.
float sdRoundBox(vec2 p, vec2 halfExtent, float radius) {
    vec2 q = abs(p) - halfExtent + radius;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

// --- ported verbatim from post/chams_plasma & chams_nebula ---
float hash21(vec2 value) {
    value = fract(value * vec2(123.34, 345.45));
    value += dot(value, value + 34.345);
    return fract(value.x * value.y);
}

float noise(vec2 value) {
    vec2 cell = floor(value);
    vec2 local = fract(value);
    local = local * local * (3.0 - 2.0 * local);
    float a = hash21(cell);
    float b = hash21(cell + vec2(1.0, 0.0));
    float c = hash21(cell + vec2(0.0, 1.0));
    float d = hash21(cell + vec2(1.0));
    return mix(mix(a, b, local.x), mix(c, d, local.x), local.y);
}

float fbm(vec2 value) {
    float result = 0.0;
    float amplitude = 0.5;
    for (int index = 0; index < 5; index++) {
        result += amplitude * noise(value);
        value *= 2.02;
        amplitude *= 0.5;
    }
    return result;
}

// Chams plasma look (post/chams_plasma.fsh), tinted by the accent color.
vec3 plasma(vec2 uv, float time, vec3 tint) {
    vec2 position = uv * vec2(6.0, 10.0);
    float firstWarp = noise(position * 0.6 + vec2(time * 0.45, -time * 0.6));
    float secondWarp = noise(position * 1.3 + firstWarp * 2.2 + vec2(-time * 0.35, time * 0.5));
    float value = noise(position + secondWarp * 2.4 + vec2(time * 0.2, -time * 0.7));
    float energy = smoothstep(0.15, 0.95, value);
    float vein = pow(energy, 2.5);
    vec3 color = mix(tint * 0.3, tint * 1.7, energy);
    color += tint * vein * 0.9;
    return color;
}

// Chams nebula look (post/chams_nebula.fsh), tinted by the accent color.
vec3 nebula(vec2 uv, float time, vec3 tint) {
    vec2 position = uv * vec2(3.0, 4.0);
    vec2 warp = vec2(
        fbm(position * 0.8 + vec2(time * 0.05, 0.0)),
        fbm(position * 0.8 + vec2(5.2, time * 0.04))
    );
    position += (warp - 0.5) * 2.2;
    float firstNoise = fbm(position * 1.2);
    float secondNoise = fbm(position * 2.6 + 4.0);
    float density = pow(smoothstep(0.30, 0.95, firstNoise * 0.7 + secondNoise * 0.3), 1.4);
    float hue = fbm(position * 0.6 + 9.0);
    vec3 tone = mix(tint * 0.55, tint * 1.7 + 0.2, smoothstep(0.18, 0.85, hue));
    return tone * density * 1.3 + tint * pow(firstNoise, 2.0) * 0.15;
}

// ShaderToy gyroid raymarch supplied for the menu background. iResolution is
// represented by the panel aspect and iTime by elapsed.
float gyroidField(vec3 p, float time) {
    float inner = dot(sin((p * 8.0).xyz), cos((p * 8.0).zxy));
    vec3 warped = p * 10.0 + vec3(0.8 * inner);
    float first = (1.0 + 0.2 * sin(p.y * 600.0))
            * dot(sin(warped.xyz), cos(warped.zxy))
            * (1.0 + sin(time + length(p.xy) * 10.0));
    vec3 detailPosition = p * (sin(time * 0.2 + p.z * 3.0) * 350.0 + 250.0);
    float detail = dot(sin(detailPosition.xyz), cos(detailPosition.zxy));
    return first
            + 0.3 * sin(time * 0.15 + p.z * 5.0 + p.y)
            * (2.0 + detail);
}

vec3 gyroidNormal(vec3 p, float time) {
    float mapped = gyroidField(p, time);
    vec2 delta = vec2(0.06 + 0.06 * sin(p.z), 0.0);
    return mapped - vec3(
        gyroidField(p - delta.xyy, time),
        gyroidField(p - delta.yxy, time),
        gyroidField(p - delta.yyx, time)
    );
}

vec3 gyroid(vec2 centeredUv, float time) {
    float distanceTravelled = 0.0;
    float distanceDelta = 1.0;
    vec3 position = vec3(0.0, 0.0, time / 4.0);
    vec3 rayDirection = normalize(vec3(centeredUv, 1.0));
    for (int index = 0; index < 90; index++) {
        if (distanceDelta <= 0.001 || distanceTravelled >= 2.0) {
            break;
        }
        distanceTravelled += distanceDelta;
        position += rayDirection * distanceTravelled;
        distanceDelta = gyroidField(position, time) * 0.02;
    }
    vec3 normal = gyroidNormal(position, time);
    float monochrome = normal.x + normal.y;
    monochrome *= smoothstep(
        0.75,
        1.05,
        1.0 / max(distanceTravelled, 0.0001)
    );
    return vec3(monochrome);
}

void main() {
    // Clip to the panel's rounded-rectangle shape first (aspect-corrected so
    // the corners stay circular), one device pixel of AA.
    vec2 boxP = (quadUv - 0.5) * aspect;
    vec2 boxHalf = 0.5 * aspect;
    float coverage = ui_coverage(sdRoundBox(boxP, boxHalf, cornerRadius));
    float alpha = primaryColor.a * coverage;
    if (alpha <= 0.001) {
        discard;
    }

    vec3 tint = primaryColor.rgb;
    vec3 color;
    if (mode == MODE_NEBULA) {
        color = nebula(quadUv, elapsed, tint);
    } else if (mode == MODE_GYROID) {
        // (coord - resolution / 2) / resolution.y
        vec2 centeredUv = (quadUv - 0.5) * aspect;
        float intensity = max(gyroid(centeredUv, elapsed).r, 0.0);
        // Preserve the original monochrome light pattern, but use the current
        // client accent as its light color instead of hardcoded white.
        color = tint * intensity;
    } else {
        color = plasma(quadUv, elapsed, tint);
    }

    color = clamp(color, 0.0, 1.0) + vec3(ui_dither(gl_FragCoord.xy));
    fragColor = vec4(clamp(color, 0.0, 1.0) * ColorModulator.rgb, alpha);
}
