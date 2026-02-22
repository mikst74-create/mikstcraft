/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
#version 330 core

uniform sampler2D tex;
layout(std140) uniform Uniforms2 {
  mat4 mvp;
  vec4 grid;
  vec4 col;
};

in vec2 quad_out;

layout(location=0) out vec4 color;

const float gridThickness = 0.2;

/*
 * Adapted from: https://www.shadertoy.com/view/wl3Sz2
 */
float filterWidth2(vec2 uv) {
  vec2 dx = dFdx(uv), dy = dFdy(uv);
  return max(length(dx), length(dy));
}
float gridSmooth() {
  vec2 q = quad_out + vec2(0.5);
  q -= floor(q);
  q = (gridThickness + 1.0) * 0.5 - abs(q - 0.5);
  float w = 5.0 * filterWidth2(quad_out);
  return 1.0 - smoothstep(0.5 - w * sqrt(gridThickness), 0.5 + w, max(q.x, q.y));
}

void main(void) {
  float gs = gridSmooth();
  //color = gs <1? (1.0 - gs) * vec3(0.0, 0.0, 0.0), 1.0):vec4(1.0, 0.0, 0.0, 1.0) ;
  color = gs<1?vec4(0,0,0,1):vec4(col.rgb,0.8);

  //vec2 texCoo = vec2( quad_out.x, 1.0-quad_out.y);
  //color = col + vec4(texture(tex, texCoo).rgb, 0.5);
}
