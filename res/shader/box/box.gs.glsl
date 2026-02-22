#version 330 core
layout (points) in;
layout(triangle_strip, max_vertices = 24) out;
//layout(points, max_vertices = 100) out;

layout(std140) uniform Uniforms {
    mat4 mvp;
    vec3 cameraPosition;
};

in VS_OUT {
    vec4 position;
    vec4 size;
    uint packedColor;
} gs_in[];


out FS_IN {
    vec3 color;
    vec2 surfacePos;
} gs_out;

void mvAndEmit(vec2 sp, vec4 p)
{
    gs_out.surfacePos=sp;
    gl_Position =  mvp *  (p - vec4(cameraPosition,0));
    EmitVertex();
}

void main(void) {
    vec4 p = gs_in[0].position;
    vec4 s = gs_in[0].size;

    gs_out.color.r = float((gs_in[0].packedColor >> 16u) & 0xFFu) / 255.0;
    gs_out.color.g = float((gs_in[0].packedColor >> 8u) & 0xFFu) / 255.0;
    gs_out.color.b = float(gs_in[0].packedColor & 0xFFu) / 255.0;

    vec4 px0y0z0 = vec4(p.x, p.y, p.z, 1);
    vec4 px0y0z1 = vec4(p.x, p.y, p.z+s.z, 1);
    vec4 px0y1z0 = vec4(p.x, p.y+s.y, p.z, 1);
    vec4 px0y1z1 = vec4(p.x, p.y+s.y, p.z+s.z, 1);
    vec4 px1y0z0 = vec4(p.x+s.x, p.y, p.z, 1);
    vec4 px1y0z1 = vec4(p.x+s.x, p.y, p.z+s.z, 1);
    vec4 px1y1z0 = vec4(p.x+s.x, p.y+s.y, p.z, 1);
    vec4 px1y1z1 = vec4(p.x+s.x, p.y+s.y, p.z+s.z, 1);

gs_out.color=vec3(0,0,1);
    mvAndEmit(vec2(0, 0), px0y0z0);
    gs_out.color.r = float((gs_in[0].packedColor >> 16u) & 0xFFu) / 255.0;
    gs_out.color.g = float((gs_in[0].packedColor >> 8u) & 0xFFu) / 255.0;
    gs_out.color.b = float(gs_in[0].packedColor & 0xFFu) / 255.0;

    mvAndEmit(vec2(0, 1), px0y1z0);
    mvAndEmit(vec2(1, 0), px1y0z0);
    mvAndEmit(vec2(1, 1), px1y1z0);
    EndPrimitive();

    mvAndEmit(vec2(0, 0), px0y0z1);
    mvAndEmit(vec2(0, 1), px0y1z1);
    mvAndEmit(vec2(1, 0), px1y0z1);
    mvAndEmit(vec2(1, 1), px1y1z1);
    EndPrimitive();

    gs_out.color=vec3(0,0,1);
    mvAndEmit(vec2(0, 0), px0y0z0);
    gs_out.color.r = float((gs_in[0].packedColor >> 16u) & 0xFFu) / 255.0;
    gs_out.color.g = float((gs_in[0].packedColor >> 8u) & 0xFFu) / 255.0;
    gs_out.color.b = float(gs_in[0].packedColor & 0xFFu) / 255.0;
    mvAndEmit(vec2(0, 1), px0y0z1);
    mvAndEmit(vec2(1, 0), px1y0z0);
    mvAndEmit(vec2(1, 1), px1y0z1);
    EndPrimitive();

    mvAndEmit(vec2(0, 0), px0y1z0);
    mvAndEmit(vec2(0, 1), px0y1z1);
    mvAndEmit(vec2(1, 0), px1y1z0);
    mvAndEmit(vec2(1, 1), px1y1z1);
    EndPrimitive();

    gs_out.color=vec3(0,0,1);
    mvAndEmit(vec2(0, 0), px0y0z0);
    gs_out.color.r = float((gs_in[0].packedColor >> 16u) & 0xFFu) / 255.0;
    gs_out.color.g = float((gs_in[0].packedColor >> 8u) & 0xFFu) / 255.0;
    gs_out.color.b = float(gs_in[0].packedColor & 0xFFu) / 255.0;
    mvAndEmit(vec2(0, 1), px0y0z1);
    mvAndEmit(vec2(1, 0), px0y1z0);
    mvAndEmit(vec2(1, 1), px0y1z1);
    EndPrimitive();

    mvAndEmit(vec2(0, 0), px1y0z0);
    mvAndEmit(vec2(0, 1), px1y0z1);
    mvAndEmit(vec2(1, 0), px1y1z0);
    mvAndEmit(vec2(1, 1), px1y1z1);
    EndPrimitive();


}