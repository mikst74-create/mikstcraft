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

    color = vec4(mix(texture(tex, fs_in.surfacePos.xy).rgb, fs_in.colorOverlay.rgb, fs_in.colorOverlay.w),0.8);
}
