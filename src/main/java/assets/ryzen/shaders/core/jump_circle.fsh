#version 330 core

#define BEATMOVE 1

const float FREQ_RANGE = 128.0;
const float PI = 3.1415;
const float RADIUS = 0.5;
const float BRIGHTNESS = 0.15;
const float SPEED = 0.5;

in vec2 uv;
out vec4 finalColor;

uniform sampler2D iChannel0;

layout(std140) uniform JumpCircleUniforms {
    vec4 fixedColor;
    vec4 params;
};

#define iTime params.x
#define iResolution vec3(1.0, 1.0, 1.0)

//convert HSV to RGB
vec3 hsv2rgb(vec3 color){
    vec4 konvert = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 calc = abs(fract(color.xxx + konvert.xyz) * 6.0 - konvert.www);
    return color.z * mix(konvert.xxx, clamp(calc - konvert.xxx, 0.0, 1.0), color.y);
}

float luma(vec3 color) {
  //return dot(color, vec3(0.299, 0.587, 0.114));
  return dot(color, vec3(0.299, 0.587, 0.5));
}

float getFrequency(float x) {
	return texture(iChannel0, vec2(floor(x * FREQ_RANGE + 1.0) / FREQ_RANGE, 0.25)).x + 0.06;
}

float getFrequency_smooth(float x) {
	float index = floor(x * FREQ_RANGE) / FREQ_RANGE;
    float next = floor(x * FREQ_RANGE + 1.0) / FREQ_RANGE;
	return mix(getFrequency(index), getFrequency(next), smoothstep(0.0, 1.0, fract(x * FREQ_RANGE)));
}

float getFrequency_blend(float x) {
    return mix(getFrequency(x), getFrequency_smooth(x), 0.5);
}

vec3 circleIllumination(vec2 fragment, float radius) {
	float distance = length(fragment);
	float ring = 1.0 / abs(distance - radius - (getFrequency_smooth(0.0)/4.50));
	
	//float brightness = distance < radius ? BRIGHTNESS * 0.3 : BRIGHTNESS;
	
	vec3 color = vec3(0.0);
	
	float angle = atan(fragment.x, fragment.y);
	color += hsv2rgb( vec3( ( angle + iTime * 2.5 ) / (PI * 2.0), 1.0, 1.0 ) ) * ring * BRIGHTNESS;
	
	float frequency = max(getFrequency_blend(abs(angle / PI)) - 0.02, 0.0);
	color *= frequency;
	
	// Black halo
	//color *= smooth//step(radius * 0.5, radius, distance);
	
	return color;
}

vec3 doLine(vec2 fragment, float radius, float x) {
	vec3 col = hsv2rgb(vec3(x * 0.23 + iTime * 0.12, 1.0, 1.0));
	
	float freq = abs(fragment.x * 0.5);
	
	col *= (1.0 / abs(fragment.y)) * BRIGHTNESS * getFrequency(freq);	
	col = col * smoothstep(radius, radius * 1.8, abs(fragment.x));
	
	return col;
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    
    vec2 fragPos = fragCoord / iResolution.xy;
	fragPos = (fragPos - 0.5) * 2.0;
    fragPos.x *= iResolution.x / iResolution.y;
    
    vec3 color = vec3(0.0,0.0,0.0);
	color += circleIllumination(fragPos, RADIUS);
    
    
    
    color += max(luma(color) - 1.0, 0.0);
    
    fragColor = vec4(color, 1.0);
}

void main() {
    vec4 color;
    mainImage(color, uv);

    float rgbMode = params.z;
    float alpha = params.w;
    if (rgbMode < 0.5) {
        float brightness = max(max(color.r, color.g), color.b);
        color.rgb = fixedColor.rgb * brightness;
    }

    float brightness = max(max(color.r, color.g), color.b);
    float visible = smoothstep(0.18, 0.48, brightness);

    // Glow edge: when the glow is on (params.y), bleed the circle color along the
    // outer rim so the white circle looks lit from outside by the glow curtain.
    float glowEdge = params.y;
    if (glowEdge > 0.5) {
        float r = length((uv - 0.5) * 2.0);
        float rim = smoothstep(0.55, 0.98, r) * (1.0 - smoothstep(0.98, 1.05, r));
        vec3 edgeColor = rgbMode < 0.5 ? fixedColor.rgb : color.rgb;
        float edge = rim * alpha * 0.9;
        finalColor = vec4(color.rgb * visible * alpha + edgeColor * edge, max(visible * alpha, edge));
        return;
    }

    if (visible <= 0.01) {
        discard;
    }
    finalColor = vec4(color.rgb * visible * alpha, visible * alpha);
}
