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
    gl_Position = vec4(0,0,0,1);
    vs_out.mvp = mvp ;
    vs_out.time = time;
}