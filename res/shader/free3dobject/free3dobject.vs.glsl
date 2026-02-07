/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 *
 * Шейдер для отрисовки произвольных 3D моделей
 * На входе буфер координат треугольников и координат на текстуре
 *
 * Отрисовка методом glDrawArrays(GL_TRIANGLES, 0, <num>); - <num> количество треугольников
 */
#version 330 core

layout(location=0) in vec4 vertexCoo;// координаты треугольника
layout(location=1) in vec4 textureCoo;// координаты текстуры:
layout(location=2) in vec4 colorOverlay;// наложение цвета

layout(std140) uniform Uniforms {
    mat4 mvp;
    vec3 pos;
};

out FS_IN {
    vec2 surfacePos;
    vec4 colorOverlay;
} vs_out;

void main(void) {
    vs_out.surfacePos = textureCoo.xy;
    vs_out.colorOverlay = colorOverlay;
    gl_Position = mvp * vec4((vertexCoo.xyz + pos), 1);
}
