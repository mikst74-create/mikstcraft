/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 *
 * Шейдер для отрисовки текстурированных прямоугольников
 * На входе буфер координат прямоугольников и координат на текстуре
 *
 * Отрисовка методом glDrawArrays(GL_POINTS, 0, <num>); - <num> количество прямоугольников
 */
#version 330 core

layout(location=0) in vec4 frameCoo;// координаты прямоугольника в -1:1 плоскости: xy - left-bottom, zw - right-top
layout(location=1) in vec4 textureCoo;// координаты текстуры: xy - left-bottom in texture, zw - right-top in texture
layout(location=2) in vec4 colorOverlay;// наложение цвета

layout(std140) uniform Uniforms {
    mat4 mvp;
    vec3 pos;
};

out GS_IN {
    vec4 gsFrameCoo;
    vec4 gsTextureCoo;
    vec4 gsColorOverlay;
} vs_out;

void main(void) {
    vs_out.gsFrameCoo = frameCoo;
    vs_out.gsTextureCoo = textureCoo;
    vs_out.gsColorOverlay = colorOverlay;
}
