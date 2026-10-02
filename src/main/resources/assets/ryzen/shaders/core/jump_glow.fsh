#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D SceneSampler;

layout(std140) uniform JumpGlowUniforms {
    vec4 glowColor;      // rgb = circle color, a = color alpha
    vec4 params;         // x = fade (0..1), y = time, z = rgb mode (0/1), w = ring progress
};

const float TAU = 6.28318530718;

vec3 hsv2rgb(vec3 c) {
    vec4 k = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + k.xyz) * 6.0 - k.www);
    return c.z * mix(k.xxx, clamp(p - k.xxx, 0.0, 1.0), c.y);
}

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

// Seamless-around-the-ring vapor: feed the noise with sin/cos of the angle so u=0
// and u=1 sample the same point (no visible seam), several octaves scrolling upward.
float vaporRing(float angle, float scale, float v, float time) {
    vec2 ring = vec2(cos(angle), sin(angle)) * scale;
    float n = noise(ring + vec2(0.0, -time * 2.0 + v * 4.0));
    n += 0.5 * noise(ring * 2.1 + vec2(0.0, -time * 3.4 + v * 6.0));
    n += 0.25 * noise(ring * 4.3 + vec2(0.0, -time * 5.0 + v * 8.0));
    return n / 1.75;
}

void main() {
    float fade = params.x;
    float time = params.y;
    float rgbMode = params.z;
    float ringProgress = params.w;

    // u = around the circle contour (wraps), v = up the curtain (0 ground, 1 top).
    float u = uv.x;
    float v = uv.y;
    float angle = u * TAU;

    // Comet tail in the vertical direction: the curtain is tallest/brightest at the
    // base and trails smoothly upward. The tail LENGTH grows with the wave progress,
    // so it stretches out behind the front as the ring expands - all in one curtain,
    // so there are no rings and no gaps.
    float tailLength = mix(0.35, 1.0, ringProgress);
    float bottomFade = smoothstep(0.0, 0.18, v);
    float trail = 1.0 - smoothstep(0.0, tailLength, v);   // smooth fade up the tail
    float vertical = bottomFade * trail;

    // Seamless roiling steam, broken into rising streaks.
    float f1 = vaporRing(angle, 3.0, v, time);
    float f2 = vaporRing(angle + 1.7, 5.0, v, time);
    float steam = smoothstep(0.2, 0.92, mix(f1, f2, 0.5));

    float streak = mix(0.65, steam, smoothstep(0.0, 0.7, v));
    float intensity = vertical * streak * fade;
    if (intensity <= 0.003) {
        discard;
    }

    vec2 screenSize = vec2(textureSize(SceneSampler, 0));
    vec2 screenUv = gl_FragCoord.xy / screenSize;

    // Strong heat-haze refraction: warp the scene behind the curtain, mostly upward.
    vec2 warp = vec2((f2 - 0.5) * 1.4, -abs(f1 - 0.5) * 2.4 - steam * 0.8);
    vec2 offset = warp * vertical * fade * 0.10;
    vec2 distortedUv = clamp(screenUv + offset, vec2(0.0), vec2(1.0));
    vec3 refracted = texture(SceneSampler, distortedUv).rgb;

    // Glow color from the jump circle. RGB mode = rainbow spread AROUND the ring (by
    // angle) drifting over time, exactly like the jump circle; otherwise fixed color.
    vec3 baseColor;
    if (rgbMode > 0.5) {
        baseColor = hsv2rgb(vec3(fract(u + time * 0.15), 1.0, 1.0));
    } else {
        // Push saturation harder so the fixed color stays vivid, not washed out.
        float lum = dot(glowColor.rgb, vec3(0.299, 0.587, 0.114));
        baseColor = clamp(mix(vec3(lum), glowColor.rgb, 2.0), 0.0, 1.0);
    }

    // Self-illuminating glow: bright colored core low on the curtain, cranked up so the
    // glow reads as a vivid, intense light rather than a faint tint.
    float core = mix(1.2, 3.2, smoothstep(0.0, 0.35, v) * (1.0 - v));
    float glowAmount = intensity * core;
    vec3 glowLight = baseColor * glowAmount * 5.0;

    vec3 color = refracted + glowLight;

    float alpha = clamp(intensity * (0.8 + 0.4 * steam) * glowColor.a, 0.0, 1.0);
    finalColor = vec4(color, alpha);
}
