#version 330 core

in vec2 particleUv;
flat in int particleShape;
in vec4 particleColor;
in float particleLife;
in float particleSeed;
in float particlePhase;
flat in float particleAdditivity;
flat in float particleGlow;

out vec4 finalColor;

uniform sampler2D DepthSampler;

layout(std140) uniform ProceduralParticleUniforms {
    vec4 GlobalParams; // time, glow, natural, softness
    vec4 ScreenParams; // width, height, reverse-z, reserved
};

float hash11(float value) {
    value = fract(value * 0.1031);
    value *= value + 33.33;
    return fract(value * (value + value));
}

float hash21(vec2 value) {
    vec3 value3 = fract(vec3(value.xyx) * 0.1031);
    value3 += dot(value3, value3.yzx + 33.33);
    return fract((value3.x + value3.y) * value3.z);
}

float dot2(vec2 value) {
    return dot(value, value);
}

float sdCircle(vec2 point, float radius) {
    return length(point) - radius;
}

float sdSegment(vec2 point, vec2 start, vec2 end) {
    vec2 pointDelta = point - start;
    vec2 segment = end - start;
    float factor = clamp(dot(pointDelta, segment) / dot(segment, segment), 0.0, 1.0);
    return length(pointDelta - segment * factor);
}

float sdBox(vec2 point, vec2 halfSize) {
    vec2 delta = abs(point) - halfSize;
    return length(max(delta, 0.0)) + min(max(delta.x, delta.y), 0.0);
}

float sdEllipse(vec2 point, vec2 radius) {
    return (length(point / radius) - 1.0) * min(radius.x, radius.y);
}

float sdStar(vec2 point, float radius, int points, float ratio) {
    float angle = 3.1415927 / float(points);
    float edgeAngle = 3.1415927 / ratio;
    vec2 angleVector = vec2(cos(angle), sin(angle));
    vec2 edgeVector = vec2(cos(edgeAngle), sin(edgeAngle));
    float sector = mod(atan(point.x, point.y), 2.0 * angle) - angle;
    point = length(point) * vec2(cos(sector), abs(sin(sector)));
    point -= radius * angleVector;
    point += edgeVector * clamp(
        -dot(point, edgeVector),
        0.0,
        radius * angleVector.y / edgeVector.y
    );
    return length(point) * sign(point.x);
}

float sdHeart(vec2 point) {
    point.x = abs(point.x);
    if (point.y + point.x > 1.0) {
        return sqrt(dot2(point - vec2(0.25, 0.75))) - 0.35355339;
    }
    return sqrt(min(
        dot2(point - vec2(0.0, 1.0)),
        dot2(point - 0.5 * max(point.x + point.y, 0.0))
    )) * sign(point.x - point.y);
}

float sdSnowflake(vec2 point, float seed) {
    float angle = atan(point.y, point.x);
    float radius = length(point);
    angle = mod(angle, 1.0471976) - 0.5235988;
    vec2 ray = vec2(cos(angle), abs(sin(angle))) * radius;
    float armLength = 0.6 + 0.18 * hash11(seed * 7.1);
    float distance = sdSegment(ray, vec2(0.04, 0.0), vec2(armLength, 0.0)) - 0.05;
    vec2 branchDirection = vec2(0.58778525, 0.80901699);
    float firstLength = 0.16 + 0.1 * hash11(seed * 13.7);
    vec2 firstOrigin = vec2(0.26 + 0.1 * hash11(seed * 3.3), 0.0);
    distance = min(
        distance,
        sdSegment(ray, firstOrigin, firstOrigin + branchDirection * firstLength) - 0.034
    );
    float secondLength = 0.11 + 0.08 * hash11(seed * 23.9);
    vec2 secondOrigin = vec2(0.46 + 0.08 * hash11(seed * 5.7), 0.0);
    distance = min(
        distance,
        sdSegment(ray, secondOrigin, secondOrigin + branchDirection * secondLength) - 0.028
    );
    return min(distance, sdCircle(point, 0.06));
}

float sdDollar(vec2 point) {
    vec2 p0 = vec2(0.3, 0.38);
    vec2 p1 = vec2(-0.02, 0.5);
    vec2 p2 = vec2(-0.3, 0.3);
    vec2 p3 = vec2(0.02, 0.06);
    vec2 p4 = vec2(0.3, -0.2);
    vec2 p5 = vec2(0.0, -0.46);
    vec2 p6 = vec2(-0.3, -0.36);
    float distance = sdSegment(point, p0, p1);
    distance = min(distance, sdSegment(point, p1, p2));
    distance = min(distance, sdSegment(point, p2, p3));
    distance = min(distance, sdSegment(point, p3, p4));
    distance = min(distance, sdSegment(point, p4, p5));
    distance = min(distance, sdSegment(point, p5, p6));
    distance -= 0.085;
    float bar = sdSegment(point, vec2(0.0, -0.6), vec2(0.0, 0.6)) - 0.042;
    return min(distance, bar);
}

void main() {
    vec2 point = particleUv;
    float distance;
    vec3 themed = vec3(1.0);
    float flicker = 1.0;
    float time = GlobalParams.x;
    float globalGlow = GlobalParams.y;

    switch (particleShape) {
        case 0:
            distance = sdStar(point, 0.66, 5, 2.7);
            break;
        case 1:
            distance = sdDollar(point);
            themed = vec3(0.45, 0.95, 0.5);
            break;
        case 2:
            distance = sdSnowflake(point, particleSeed);
            themed = vec3(0.82, 0.93, 1.0);
            break;
        case 3:
            distance = length(point) - 0.28;
            break;
        case 4: {
            vec2 bodyPoint = point - vec2(0.0, -0.08);
            float body = sdEllipse(bodyPoint, vec2(0.62, 0.5));
            float lobes = min(
                sdEllipse(bodyPoint - vec2(-0.27, 0.0), vec2(0.34, 0.46)),
                sdEllipse(bodyPoint - vec2(0.27, 0.0), vec2(0.34, 0.46))
            );
            float pumpkin = min(body, lobes);
            vec2 stemPoint = point - vec2(0.0, 0.45);
            float stem = sdBox(
                vec2(stemPoint.x - stemPoint.y * 0.3, stemPoint.y),
                vec2(0.05, 0.13)
            ) - 0.02;
            distance = min(pumpkin, stem);
            themed = stem < pumpkin
                    ? vec3(0.35, 0.6, 0.25)
                    : vec3(1.0, 0.55, 0.15) * (0.84 + 0.16 * cos(bodyPoint.x * 10.0));
            break;
        }
        case 5: {
            vec2 heartPoint = vec2(point.x, point.y + 0.745) / 1.35;
            distance = sdHeart(heartPoint) * 1.35;
            themed = vec3(1.0, 0.3, 0.42);
            break;
        }
        case 6:
            distance = sdStar(point, 0.8, 4, 3.5);
            flicker = 0.7 + 0.3 * sin(time * 8.0 + particlePhase);
            break;
        case 7: {
            float radius = length(point);
            float wobble = 0.9
                    + 0.12 * sin(time * 5.0 + particlePhase) * globalGlow;
            distance = radius - 0.25 * wobble;
            vec3 hot = vec3(1.0, 0.93, 0.55);
            vec3 middle = vec3(1.0, 0.45, 0.12);
            vec3 cold = vec3(0.5, 0.1, 0.03);
            themed = mix(hot, middle, smoothstep(0.0, 0.55, particleLife));
            themed = mix(themed, cold, smoothstep(0.55, 1.0, particleLife));
            flicker = 0.8 + 0.25 * sin(time * 7.0 + particlePhase * 1.7);
            break;
        }
        default:
            distance = sdCircle(point, 0.5);
            break;
    }

    vec3 tint = mix(vec3(1.0), themed, GlobalParams.z);
    float coreGlow = particleGlow * globalGlow;
    float antialias = fwidth(distance) * 1.4 + 1e-4;
    float core = 1.0 - smoothstep(-antialias, antialias, distance);
    float halo = exp(-max(distance, 0.0) * 5.0) * coreGlow;
    float edge = max(abs(point.x), abs(point.y));
    float edgeFade = 1.0 - smoothstep(0.82, 1.0, edge);

    vec2 screenUv = gl_FragCoord.xy / ScreenParams.xy;
    float sceneDepth = texture(DepthSampler, screenUv).r;
    float depthDifference;
    float clearDepth;
    if (ScreenParams.z > 0.5) {
        depthDifference = gl_FragCoord.z - sceneDepth;
        clearDepth = 1.0 - step(0.000001, sceneDepth);
    } else {
        depthDifference = sceneDepth - gl_FragCoord.z;
        clearDepth = step(0.999999, sceneDepth);
    }
    if (clearDepth < 0.5 && depthDifference < -0.000001) {
        discard;
    }
    float soft = clearDepth > 0.5
            ? 1.0
            : smoothstep(0.0, GlobalParams.w, max(depthDifference, 0.0));

    float alpha = clamp(core + halo, 0.0, 1.0)
            * edgeFade
            * particleColor.a
            * soft;
    if (alpha <= 0.004) {
        discard;
    }

    float brightness = 1.0
            + coreGlow * exp(-dot2(point) * 6.0) * 1.4;
    brightness *= mix(1.0, flicker, globalGlow);
    vec3 color = particleColor.rgb * tint * brightness;
    float additive = particleAdditivity * globalGlow;
    vec3 premultiplied = color * alpha;
    premultiplied += (
        hash21(gl_FragCoord.xy + vec2(particleSeed * 491.0, fract(time) * 113.0))
        - 0.5
    ) / 255.0;
    finalColor = vec4(premultiplied, alpha * (1.0 - additive));
}
