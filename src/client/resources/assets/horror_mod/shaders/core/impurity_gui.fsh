#version 330
#extension GL_ARB_separate_shader_objects : require

#ifndef HORROR_MOD_HORROR_GLSL
#include <horror_mod:horror.glsl>
#endif

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};

uniform sampler2D Sampler0;

layout(location = 0) in vec2 texCoord0;
layout(location = 1) in vec4 vertexColor;

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 center = texture(Sampler0, texCoord0);
    if (center.a < 0.04) {
        discard;
    }
    float strength = clamp(1.0 - vertexColor.a, 0.0, 1.0);
    vec2 shift = vec2(0.045, -0.02) * strength;
    vec3 chroma = vec3(
            texture(Sampler0, texCoord0 + shift).r,
            center.g,
            texture(Sampler0, texCoord0 - shift).b
    );
    vec3 body = mix(center.rgb, chroma, strength);
    fragColor = vec4(body, center.a) * vec4(ColorModulator.rgb, 1.0) * HORROR_MOD_PASS;
}
