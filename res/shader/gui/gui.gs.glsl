#version 330 core
layout (points) in;
layout(triangle_strip, max_vertices = 4) out;



layout(std140) uniform Uniforms {
  mat4 mvp;
};

// input from vertex shader
in GS_IN {
  vec4 gsFrameCoo; // xy - left-bottom, zw - right-top
  vec4 gsTextureCoo; // xy - left-bottom in texture, zw - right-top in texture
} gs_in[];


out FS_IN {
  vec2 surfacePos;
} gs_out;

void main() {
   vec4 gsFrameCoo=gs_in[0].gsFrameCoo;
   vec4 gsTextureCoo=gs_in[0].gsTextureCoo;

   gl_Position = mvp * vec4(gsFrameCoo.xy, 0.0, 1.0);
   gs_out.surfacePos = vec2(gsTextureCoo.xy);
   EmitVertex();

   gl_Position = mvp * vec4(gsFrameCoo.zy, 0.0, 1.0);
   gs_out.surfacePos = vec2(gsTextureCoo.zy);
   EmitVertex();

   gl_Position = mvp * vec4(gsFrameCoo.xw, 0.0, 1.0);
   gs_out.surfacePos = vec2(gsTextureCoo.xw);
   EmitVertex();

   gl_Position = mvp * vec4(gsFrameCoo.zw, 0.0, 1.0);
   gs_out.surfacePos = vec2(gsTextureCoo.zw);
   EmitVertex();

   EndPrimitive();

}