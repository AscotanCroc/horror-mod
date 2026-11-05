#version 330
#extension GL_ARB_separate_shader_objects : require

#ifndef MINECRAFT_GLOBALS_GLSL
#include <minecraft:globals.glsl>
#endif
#ifndef HORROR_MOD_HORROR_GLSL
#include <horror_mod:horror.glsl>
#endif

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform LecternConfig {
    float Strength;
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

void main() {
    float seconds = GameTime * 1200.0;
    float aspect = InSize.x / max(InSize.y, 1.0);
    vec2 p = texCoord - 0.5;
    p.x *= aspect;

    float r = length(p);
    float ang = atan(p.y, p.x);
    float spin = seconds * (2.2 + 0.6 * sin(seconds * 0.37));
    float whirl = ang
            + spin * Strength
            + sin(r * 22.0 - seconds * 6.5) * 1.8
            + sin(ang * 5.0 + seconds * 3.1) * 0.7
            + r * (11.0 + 6.0 * sin(seconds * 0.8));
    float radius = r * (0.45 + 0.7 * sin(seconds * 1.3 + ang * 2.0));
    radius += sin(whirl * 3.0 - seconds * 4.0) * 0.08;

    vec2 uv = vec2(cos(whirl), sin(whirl)) * radius;
    uv.x /= aspect;
    uv = fract(uv + 0.5 + vec2(sin(seconds * 0.9), cos(seconds * 0.7)) * 0.15);

    vec2 uv2 = vec2(cos(whirl * 1.7 - spin), sin(whirl * 0.6 + r * 30.0)) * (0.2 + r * 1.4);
    uv2.x /= aspect;
    uv2 = fract(uv2 + 0.5);

    float luma = dot(texture(InSampler, uv).rgb, vec3(0.299, 0.587, 0.114));
    float luma2 = dot(texture(InSampler, uv2).rgb, vec3(0.299, 0.587, 0.114));
    float bands = sin(whirl * 14.0 - seconds * 9.0 + r * 70.0);
    float rings = sin(r * 90.0 - seconds * 7.0 + ang * 4.0);
    float mono = luma * 0.35 + luma2 * 0.25 + bands * 0.28 + rings * 0.22;
    mono = smoothstep(0.08, 0.72, mono * 0.5 + 0.5);
    mono = floor(mono * 6.0 + 0.5) / 6.0;
    mono *= 0.55 + 0.45 * sin(seconds * 14.0 + r * 20.0);

    fragColor = vec4(vec3(clamp(mono, 0.0, 1.0)), 1.0) * HORROR_MOD_PASS;
}
