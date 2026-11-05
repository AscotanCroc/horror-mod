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
    vec2 shift = vec2(0.006, -0.003);
    vec3 chroma = vec3(
            texture(Sampler0, texCoord0 + shift).r,
            center.g,
            texture(Sampler0, texCoord0 - shift).b
    );
    vec3 rainbow = vertexColor.rgb;
    vec3 body = mix(chroma, rainbow, 0.82);
    fragColor = vec4(body, center.a) * ColorModulator * HORROR_MOD_PASS;
}
