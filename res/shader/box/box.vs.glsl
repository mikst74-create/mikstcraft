#version 330 core
layout (location = 0) in vec4 in_position;// <vec2 pos, vec2 tex>
layout (location = 1) in vec4 in_size;// <vec2 pos, vec2 tex>
layout (location = 2) in uint in_packedColor;// <vec2 pos, vec2 tex>


out VS_OUT {
    vec4 position;
    vec4 size;
    uint packedColor;
} vs_out;

void main(void)
{
    gl_Position = vec4(0,0,0,1);
    vs_out.position=in_position;
    vs_out.size=in_size;
    vs_out.packedColor=in_packedColor;
}