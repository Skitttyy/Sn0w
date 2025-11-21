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
out vec4 fragColor;

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
        color = mix(vec4(texture(DiffuseSampler, texCoord).rgb, fillColor.a), bestOpaqueColor, glowBlendFactor); // Blend based on glow intensity
    }
    return color;
}

vec4 calculateOuterOutline(vec4 color) { // Apply outer outline
    for (int x = -lineWidth; x <= lineWidth; x++) {
        for (int y = -lineWidth; y <= lineWidth; y++) {
            vec4 sampledTexel = texture(DiffuseSampler, texCoord + texelSize * vec2(x, y));
            if (sampledTexel.a > 0.0f) {
                color = sampledTexel; // Outline texel
            }
        }
    }
    return color;
}

vec4 calculateOuterGlow(vec4 color) {
    if (glowThickness != 0.0f && color.a == 0.0f) { // Apply outer glow-based transparency.
        vec4 bestOpaqueColor = vec4(0.0f);
        float glowIntensity = computeGlowIntensity(bestOpaqueColor);
        color = vec4(bestOpaqueColor.rgb, glowIntensity);
    }
    return color;
}

void main() {
    vec4 centerTexel = texture(DiffuseSampler, texCoord);
    vec4 color = centerTexel;


    if (color.a != 0.0f) {

        color = calculateInnerGlow(vec4(color.rgb, 0.0f));
    } else {
        color = calculateOuterGlow(calculateOuterOutline(color));
    }
    fragColor = color;
}