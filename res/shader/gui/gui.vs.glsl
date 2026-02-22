/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
#version 330 core

layout(location=0) in vec4 frameCoo; // xy - left-bottom, zw - right-top
layout(location=1) in vec4 textureCoo; // xy - left-bottom in texture, zw - right-top in texture

layout(std140) uniform Uniforms {
  mat4 mvp;
};

uniform sampler2D texture;

out GS_IN {
  vec4 gsFrameCoo;
  vec4 gsTextureCoo;
} vs_out;

void main(void) {
  //gl_Position = vec4(0, 0, 0, 1.0);
  vs_out.gsFrameCoo = frameCoo;
  vs_out.gsTextureCoo = textureCoo;
}
