#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D SceneSampler;
uniform sampler2D DepthSampler;

layout(std140) uniform PointLightUniforms {
    mat4 InvViewProjection;
    vec4 LightParams;
};

layout(std140) uniform PointLights {
    vec4 PosRadius[16];
    vec4 ColorFlicker[16];
    vec4 Extra[16];
};

vec3 reconstructPosition(vec2 coords, float depth) {
    float clipZ = LightParams.w > 0.5 ? depth : depth * 2.0 - 1.0;
    vec4 ndc = vec4(coords * 2.0 - 1.0, clipZ, 1.0);
    vec4 position = InvViewProjection * ndc;
    return position.xyz / position.w;
}

float hash11(float value) {
    value = fract(value * 0.1031);
    value *= value + 33.33;
    return fract(value * (value + value));
}

float hash21(vec2 value) {
    vec3 p3 = fract(vec3(value.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

void main() {
    vec3 scene = texture(SceneSampler, uv).rgb;
    float depth = texture(DepthSampler, uv).r;
    bool clearDepth = LightParams.w > 0.5
        ? depth <= 0.000001
        : depth >= 0.999999;
    if (clearDepth) {
        finalColor = vec4(scene, 1.0);
        return;
    }

    vec3 position = reconstructPosition(uv, depth);
    vec3 derivativeX = dFdx(position);
    vec3 derivativeY = dFdy(position);
    vec3 normal = normalize(cross(derivativeX, derivativeY));
    if (dot(normal, -position) < 0.0) {
        normal = -normal;
    }
    float edgeFade = 1.0 - smoothstep(
        0.02,
        0.06,
        (length(derivativeX) + length(derivativeY)) / max(length(position), 1.0)
    );

    float time = LightParams.x;
    float intensity = LightParams.y;
    int lightCount = int(LightParams.z + 0.5);
    vec3 light = vec3(0.0);
    for (int index = 0; index < lightCount; index++) {
        vec3 toLight = PosRadius[index].xyz - position;
        float radius = PosRadius[index].w;
        float distanceToLight = length(toLight);
        if (distanceToLight >= radius) {
            continue;
        }

        vec3 direction = toLight / max(distanceToLight, 1.0e-4);
        float diffuse = clamp(dot(normal, direction), 0.0, 1.0);
        float lambert = mix(0.65, mix(0.3, 1.0, diffuse), edgeFade);
        float falloff = 1.0 - distanceToLight / radius;
        falloff = falloff * falloff * (3.0 - 2.0 * falloff);
        falloff *= falloff;

        float flickerAmount = ColorFlicker[index].a;
        float flicker = 1.0;
        if (flickerAmount > 0.001) {
            float phase = Extra[index].x;
            float wave = sin(time * 9.0 + phase) * 0.5
                    + sin(time * 23.7 + phase * 1.7) * 0.3;
            float spark = hash11(floor(time * 12.0) + phase) - 0.5;
            flicker = 1.0 - flickerAmount * (0.22 + 0.16 * wave + 0.12 * spark);
        }
        light += ColorFlicker[index].rgb * (falloff * lambert * flicker);
    }

    light *= intensity;
    vec3 lit = scene * (1.0 + light * 2.4) + light * light * 0.22;
    float maximum = max(lit.r, max(lit.g, lit.b));
    if (maximum > 0.8) {
        lit *= (0.8 + 0.2 * (1.0 - exp((0.8 - maximum) * 5.0))) / maximum;
    }
    lit += (hash21(gl_FragCoord.xy) - 0.5) / 255.0;
    finalColor = vec4(lit, 1.0);
}
