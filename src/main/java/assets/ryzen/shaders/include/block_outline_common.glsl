float hash11(float p) {
    p = fract(p * 0.1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

float hash21(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

vec3 hash33(vec3 p3) {
    p3 = fract(p3 * vec3(0.1031, 0.1030, 0.0973));
    p3 += dot(p3, p3.yxz + 33.33);
    return fract((p3.xxy + p3.yxx) * p3.zyx);
}

float valueNoise(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float n000 = hash33(i).x;
    float n100 = hash33(i + vec3(1.0, 0.0, 0.0)).x;
    float n010 = hash33(i + vec3(0.0, 1.0, 0.0)).x;
    float n110 = hash33(i + vec3(1.0, 1.0, 0.0)).x;
    float n001 = hash33(i + vec3(0.0, 0.0, 1.0)).x;
    float n101 = hash33(i + vec3(1.0, 0.0, 1.0)).x;
    float n011 = hash33(i + vec3(0.0, 1.0, 1.0)).x;
    float n111 = hash33(i + vec3(1.0)).x;
    float nx00 = mix(n000, n100, f.x);
    float nx10 = mix(n010, n110, f.x);
    float nx01 = mix(n001, n101, f.x);
    float nx11 = mix(n011, n111, f.x);
    return mix(mix(nx00, nx10, f.y), mix(nx01, nx11, f.y), f.z);
}

float fbm(vec3 p) {
    float sum = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;
    for (int i = 0; i < 6; i++) {
        sum += amplitude * valueNoise(p * frequency);
        frequency *= 2.02;
        amplitude *= 0.5;
    }
    return sum;
}

float starGlow(vec3 direction, float scale, float density, float time) {
    vec3 p = direction * scale;
    vec3 cell = floor(p);
    float result = 0.0;
    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            for (int z = -1; z <= 1; z++) {
                vec3 id = cell + vec3(x, y, z);
                // Any star in this cell would contribute < 1e-4 (well below
                // 8-bit quantization); skip the hash work entirely.
                if (length(p - id - 0.5) > 1.35) {
                    continue;
                }
                vec3 h = hash33(id);
                if (h.x > density) {
                    continue;
                }
                vec3 center = id + 0.2 + 0.6 * h;
                float distanceToStar = length(p - center);
                float brightness = pow(hash11(h.y + 1.7), 4.0);
                float twinkle = 0.6 + 0.4 * sin(time * 2.0 + h.z * 50.0);
                float core = smoothstep(0.06, 0.0, distanceToStar);
                float halo = exp(-distanceToStar * 12.0) * 0.5;
                result += (core + halo) * (0.4 + brightness * 2.6) * twinkle;
            }
        }
    }
    return result;
}

vec3 ditherRgb(vec2 fragCoord) {
    return vec3((hash21(fragCoord) - 0.5) / 255.0);
}
