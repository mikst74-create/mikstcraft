#version 330 core
layout (location = 0) in int vex_point; // <vec2 pos, vec2 tex>

layout(std140) uniform Uniforms {
  mat4 mvp;
  vec4 time;
};

out VS_OUT {
    mat4 mvp;
    vec4 time;
} vs_out;

void main()
{
    vec3 vertex =  vec3(float((gl_VertexID & 0x000F)),  4+float((gl_VertexID & 0xFF00) >> 8), float((gl_VertexID & 0x00F0) >> 4));
    gl_Position = vec4(vertex.xyz,1);
    vs_out.mvp = mvp ;
    vs_out.time = time;
}