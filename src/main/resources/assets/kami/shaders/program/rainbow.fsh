/*
 * Copyright (C) 2016-2025 Future Development and/or its affiliates. All rights reserved.
 */

#version 150

uniform sampler2D DiffuseSampler;

uniform int glowThickness;
uniform int glowSampleStep;

uniform float glowIntensityScale;
uniform vec4 fillColor;
in vec2 texCoord;
uniform vec2 texelSize;
uniform vec2 resolution;
uniform float time;
uniform float saturation;
uniform float lightness;
uniform float factor;
out vec4 fragColor;


vec3 hslToRgb(float h, float s, float l) {
    float c = (1.0 - abs(2.0 * l - 1.0)) * s;
    float x = c * (1.0 - abs(mod(h * 6.0, 2.0) - 1.0));
    float m = l - c / 2.0;
    vec3 rgb;

    if (h < 1.0 / 6.0) {
        rgb = vec3(c, x, 0.0);
    } else if (h < 2.0 / 6.0) {
        rgb = vec3(x, c, 0.0);
    } else if (h < 3.0 / 6.0) {
        rgb = vec3(0.0, c, x);
    } else if (h < 4.0 / 6.0) {
        rgb = vec3(0.0, x, c);
    } else if (h < 5.0 / 6.0) {
        rgb = vec3(x, 0.0, c);
    } else {
        rgb = vec3(c, 0.0, x);
    }

    return rgb + vec3(m);
}


vec4 selectBestOpaqueColor(inout vec4 bestOpaqueColor, vec4 texel) {
    bestOpaqueColor = texel.a != 0.0 ? texel : bestOpaqueColor;
    return texel;
}

float computeGlowIntensity(inout vec4 bestOpaqueColor) {
    int glowSampleRadius = glowSampleStep * glowThickness;
    vec2 neighborOffsets[8] = vec2[](
        vec2(glowSampleRadius, 0),    // Right
        vec2(-glowSampleRadius, 0),   // Left
        vec2(0, glowSampleRadius),    // Up
        vec2(0, -glowSampleRadius),   // Down
        vec2(glowSampleRadius, -glowSampleRadius),  // Bottom-Right
        vec2(-glowSampleRadius, glowSampleRadius),  // Top-Left
        vec2(glowSampleRadius, glowSampleRadius),   // Top-Right
        vec2(-glowSampleRadius, -glowSampleRadius)  // Bottom-Left
    );
    float accumulatedGlow = 0.0;
    for (int i = 0; i < 8; i++) { // Check neighboring texels
        accumulatedGlow += sign(selectBestOpaqueColor(bestOpaqueColor, texture(DiffuseSampler, texCoord + texelSize * neighborOffsets[i])).a);
    }
    float glowContributionWeight = float((glowThickness * glowThickness) + glowThickness) * 4.0f; // (glowThickness^2+glowThickness)*4
    for (int x = -glowSampleRadius; x <= glowSampleRadius; x += glowSampleStep) {
        for (int y = -glowSampleRadius; y <= glowSampleRadius; y += glowSampleStep) {
            if (x == 0 && y == 0) {
                continue; // Skip center texel
            }
            if ((abs(x) == glowSampleRadius && abs(y) == glowSampleRadius) ||
                (abs(x) == glowSampleRadius && y == 0) ||
                (abs(y) == 0 && x == 0)) {
                continue; // Skip corners and edges
            }
            accumulatedGlow += sign(selectBestOpaqueColor(bestOpaqueColor, texture(DiffuseSampler, texCoord + texelSize * vec2(x, y))).a);
        }
    }
    float scaledGlowIntensity = clamp(accumulatedGlow / glowContributionWeight, 0.0f, 1.0f);
    return glowIntensityScale * scaledGlowIntensity;
}

const int lineWidth = 1;

vec4 calculateInnerGlow(vec4 color) { // Apply inner glow effect
    if (glowThickness != 0.0f) {
        vec4 bestOpaqueColor = color;
        float glowIntensity = computeGlowIntensity(bestOpaqueColor);
        float glowBlendFactor = glowIntensityScale - glowIntensity;
        color = mix(vec4(color.rgb, fillColor.a), bestOpaqueColor, glowBlendFactor); // Blend based on glow intensity
    }
    return color;
}

vec4 calculateOuterOutline(vec4 color) { // Apply outer outline
    for (int x = -lineWidth; x <= lineWidth; x++) {
        for (int y = -lineWidth; y <= lineWidth; y++) {
            vec4 sampledTexel = texture(DiffuseSampler, texCoord + texelSize * vec2(x, y));
            if (sampledTexel.a > 0.0f) {
                color = vec4(color.rgb, sampledTexel.a); // Outline texel
            }
        }
    }
    return color;
}

vec4 calculateOuterGlow(vec4 color) {
    if (glowThickness != 0.0f && color.a == 0.0f) { // Apply outer glow-based transparency.
        vec4 bestOpaqueColor = vec4(color.rgb, 0.0f);
        float glowIntensity = computeGlowIntensity(bestOpaqueColor);
        color = vec4(color.rgb, glowIntensity);
    }
    return color;
}

void main() {
    vec4 centerTexel = texture(DiffuseSampler, texCoord);
    vec4 color = centerTexel;

    float hue = mod((texCoord.x + texCoord.y) * factor + time * 0.5, 1.0);
    vec3 rgbColor = hslToRgb(hue, saturation, lightness);
    color = vec4(rgbColor, color.a);
    if (color.a != 0.0f) {
        color = calculateInnerGlow(vec4(color.rgb, 0.0f));
    } else {
        color = calculateOuterGlow(calculateOuterOutline(color));
    }
    fragColor = vec4(rgbColor, color.a);
}