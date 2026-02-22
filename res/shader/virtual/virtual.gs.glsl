#version 330 core
layout (points) in;
layout(points, max_vertices = 27) out;
//layout(triangle_strip, max_vertices = 27) out;

const float scale2=1;

in VS_OUT {
    mat4 mvp;
    vec4 time;
} gs_in[];

vec4 mv(vec4 p, vec4 m)
{
  return gs_in[0].mvp * (p + m);
}

void main() {    
    gl_PointSize = 20;

// правильная последовательность для локального сдвига от центральной точки вокселя
   // gl_Position = invMat * gl_in[0].gl_Position; // возврат в мировые координаты
   // gl_Position = gl_Position + vec4(1,1,0,1); // сдвиг на XYZ
   // gl_Position = inverse(invMat) * gl_Position; // расчет экранных координат


    float scale = 0.4;//0.5+0.3*fract(sin(dot(gl_in[0].gl_Position.xy,
                      //                     vec2(12.9898,78.233)))
                        //           * 43758.5453123);
    vec4 gp = gl_in[0].gl_Position;

    gl_Position = mv(gp, vec4(0,sin( gs_in[0].time.x*4E-2)*0.5,-0.4,0));
    EmitVertex();
   gl_Position = mv(gp, vec4(-0.3,sin( gs_in[0].time.x*3.8E-2)*0.5,0,0));
    EmitVertex();
   gl_Position = mv(gp, vec4(0.2,sin( gs_in[0].time.x*4.2E-2)*0.5,0.4,0));
    EmitVertex();
    EndPrimitive();


   gl_Position = mv(gp, vec4(-0.2,sin( gs_in[0].time.x*4.6E-2)*0.5,0.4,0));
    EmitVertex();
   gl_Position = mv(gp, vec4(0.4,sin( gs_in[0].time.x*3.4E-2)*0.5,0.1,0));
    EmitVertex();
   gl_Position = mv(gp, vec4(-0.2,sin( gs_in[0].time.x*4.1E-2)*0.5,-0.4,0));
    EmitVertex();
    EndPrimitive();

 /*
 // Сетка куба 3х3х3
    gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(+scale, +scale, -scale, 1.0));
    EmitVertex();
    gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(+scale, +scale, 0, 0.0));
    EmitVertex();
    gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(+scale, +scale, +scale, 0.0));
    EmitVertex();
    gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(+scale, 0, -scale, 0.0));
    EmitVertex();
    gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(+scale, 0, 0, 0.0));
    EmitVertex();
    gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(+scale, 0, +scale, 0.0));
    EmitVertex();
    gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(+scale, -scale, -scale, 0.0));
    EmitVertex();
    gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(+scale, -scale, 0, 0.0));
    EmitVertex();
    gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(+scale, -scale, +scale, 0.0));
    EmitVertex();


    gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(0, +scale, -scale, 0.0));
    EmitVertex();
    gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(0, +scale, 0, 0.0));
    EmitVertex();
    gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(0, +scale, +scale, 0.0));
    EmitVertex();
    gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(0, 0, -scale, 0.0));
    EmitVertex();
    gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(0, 0, 0, 0.0));
    EmitVertex();
    gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(0, 0, +scale, 0.0));
    EmitVertex();
    gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(0, -scale, -scale, 0.0));
    EmitVertex();
    gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(0, -scale, 0, 0.0));
    EmitVertex();
    gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(0, -scale, +scale, 0.0));
    EmitVertex();

     gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(-scale, +scale, -scale, 0.0));
     EmitVertex();
     gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(-scale, +scale, 0, 0.0));
     EmitVertex();
     gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(-scale, +scale, +scale, 0.0));
     EmitVertex();
     gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(-scale, 0, -scale, 0.0));
     EmitVertex();
     gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(-scale, 0, 0, 0.0));
     EmitVertex();
     gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(-scale, 0, +scale, 0.0));
     EmitVertex();
     gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(-scale, -scale, -scale, 0.0));
     EmitVertex();
     gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(-scale, -scale, 0, 0.0));
     EmitVertex();
     gl_Position = gs_in[0].mvp * (invMat * gl_in[0].gl_Position + vec4(-scale, -scale, +scale, 0.0));
     EmitVertex();
*/

}