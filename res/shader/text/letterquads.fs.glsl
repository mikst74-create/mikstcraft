/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
#version 330 core

uniform sampler2D tex;

in FS_IN {
  vec2 surfacePos;
  vec4 colorOverlay;
} fs_in;

layout(location=0) out vec4 color;

void main(void) {

  color = vec4(fs_in.colorOverlay.rgb, 1-texture(tex, fs_in.surfacePos).r);
//  color = vec4(texture(tex, fs_in.surfacePos).rgb, 1);
}
