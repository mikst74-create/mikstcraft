/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
#version 330 core
#pragma {{DEFINES}}

layout(std140) uniform Uniforms {
    mat4 mvp;//16*4 = 64bytes
//    vec4 tmvp;// 4*4 = 16 bytes
    vec4 camPos;// 2*4 = 8 bytes
//total 88 bytes
};


// Запись в этот униформ делается тут
//org/lwjgl/demo/mikstcraft/render/RenderedWorldArea.java:324
//      glActiveTexture(GL_TEXTURE0);
//        glBindTexture(GL_TEXTURE_BUFFER, PerFaceBuffers.chunkInfoTexture);
// То есть этот униформ идет первый в списке создания программы
// addUniformLocation("chunkInfo", 0);
// Поэтому линкуется на слот 0
// и чтобы в него записать (он притворяется тектурой) нужно писать в нулевую текстуру
// Ох уж этот openGl

uniform isamplerBuffer chunkInfo;

const float aos[4] = float[4](AO_FACTORS);

// данные сюда передаются через буфер vertexData (VAO-VBO)
layout(location=0) in uvec4 positionAndType;
layout(location=1) in uvec2 sideAndAoFactors;
layout(location=2) in uint chunkIndex;

out FS_IN {
    vec4 ao;
    vec3 surfacePos;
} vs_out;

centroid out vec2 uv;

vec3 offset() {
    // старший байт sideAndAoFactors
    uint s = sideAndAoFactors.x;

    /*
  Формирует последовательность векторов
  -1,-1    1,-1     -1, 1     1, 1
  Чтобы сформировать квадрат, последовательность точек такая
  2     4
  | \   |
  |   \ |
  1     3
  */
    vec3 r = vec3(gl_VertexID & 1, // X = 0 для четных вертексов, 1 - для нечетных. то есть последовательность 010101010...
    gl_VertexID >> 1 & 1, // Y = 0 для каждого второго/ последовательность 0011001100...
    0.5)// Z всегда 0.5
    * 2.0// X и/или Y станут равны 2, Z =1
    - vec3(1.0);// и вычесть вектор 1,1,1. хм

    // далее поворот в нужную плоскость
    // 0xy - при s=1, поперек оси X
    // y0x - при s=2, поперек оси Y
    // xy0 - при s=4, поперек оси Z
    return mix(r.zxy, mix(r.yzx, r.xyz, step(4.0, float(s))), step(2.0, float(s)));
}

vec3 surfpos() {
    uint s = sideAndAoFactors.x;
    // Позиция на текстуре?
    // yz - при s=1, поперек оси X
    // zx - при s=2, поперек оси Y
    // xy - при s=4, поперек оси Z
    //  return mix(positionAndType.yz, mix(positionAndType.zx, positionAndType.xy, step(4.0, float(s))), step(2.0, float(s)));
    return vec3(mix(positionAndType.yz, mix(positionAndType.zx, positionAndType.xy, step(4.0, float(s))), step(2.0, float(s))), positionAndType.w);

}

// Положение чанка относительно камеры (только сдвиг, без поворотов)
vec3 relChunkPos() {
    ivec4 ci;
    ci = texelFetch(chunkInfo, int(chunkIndex));
    return vec3(ci.x, ci.y, ci.z) - camPos.xyz;
}

void main(void) {
    uv = vec2(gl_VertexID & 1, gl_VertexID >> 1 & 1);
    vs_out.ao = vec4(aos[sideAndAoFactors.y & 3u],
    aos[sideAndAoFactors.y >> 2u & 3u],
    aos[sideAndAoFactors.y >> 4u & 3u],
    aos[sideAndAoFactors.y >> 6u & 3u]);
    vs_out.surfacePos = surfpos();
    vec3 p = positionAndType.xyz + relChunkPos();
//    float w = dot(tmvp, vec4(p, 1.0));
    gl_Position = mvp * vec4(p, 1.0);
//    gl_Position = mvp * vec4(p + offset() * 2E-4 * w, 1.0);
    //  texCoords = gl_Position.xy;

}
