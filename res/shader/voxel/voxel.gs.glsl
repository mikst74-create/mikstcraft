#version 330 core
layout (points) in;
layout(triangle_strip, max_vertices = 24) out;
//layout(points, max_vertices = 25) out;

in VS_OUT {
    mat4 mvp;
    vec4 time;
} gs_in[];

void mvAndEmit(vec4 p, vec4 m)
{
  gl_Position = gs_in[0].mvp * (p + m);
  EmitVertex();
}

const float sideStep = 0.45; // расстояние от центра вокселя до грани (вообще-то оно 0.5, но для отладки можно уменьшить)

void main() {    
    gl_PointSize = 20;

// правильная последовательность для локального сдвига от центральной точки вокселя
   // gl_Position = invMat * gl_in[0].gl_Position; // возврат в мировые координаты
   // gl_Position = gl_Position + vec4(1,1,0,1); // сдвиг на XYZ
   // gl_Position = inverse(invMat) * gl_Position; // расчет экранных координат


    float s =  0.5-(sin(gs_in[0].time.x*8E-2)*0.03+0.1);
    vec4 gp = gl_in[0].gl_Position;

    mvAndEmit(gp, vec4(+sideStep,+s,+s,0));
    mvAndEmit(gp, vec4(+sideStep,+s,-s,0));
    mvAndEmit(gp, vec4(+sideStep,-s,-s,0));
    EndPrimitive();
    mvAndEmit(gp, vec4(+sideStep,-s,-s,0));
    mvAndEmit(gp, vec4(+sideStep,-s,+s,0));
    mvAndEmit(gp, vec4(+sideStep,+s,+s,0));
    EndPrimitive();
 /*   mvAndEmit(gp, vec4(-sideStep,+s,+s,0));
    mvAndEmit(gp, vec4(-sideStep,+s,-s,0));
    mvAndEmit(gp, vec4(-sideStep,-s,+s,0));
    mvAndEmit(gp, vec4(-sideStep,-s,-s,0));
    EndPrimitive();
*/

 /*   mvAndEmit(gp, vec4(+s,+sideStep,+s,0));
    mvAndEmit(gp, vec4(+s,+sideStep,-s,0));
    mvAndEmit(gp, vec4(-s,+sideStep,+s,0));
    mvAndEmit(gp, vec4(-s,+sideStep,-s,0));
    EndPrimitive();*/
  /*  mvAndEmit(gp, vec4(+s,-sideStep,+s,0));
    mvAndEmit(gp, vec4(+s,-sideStep,-s,0));
    mvAndEmit(gp, vec4(-s,-sideStep,+s,0));
    mvAndEmit(gp, vec4(-s,-sideStep,-s,0));
    EndPrimitive();
*/
  /*  mvAndEmit(gp, vec4(+s,+s,+sideStep,0));
    mvAndEmit(gp, vec4(+s,-s,+sideStep,0));
    mvAndEmit(gp, vec4(-s,+s,+sideStep,0));
    mvAndEmit(gp, vec4(-s,-s,+sideStep,0));
    EndPrimitive();*/
 /*   mvAndEmit(gp, vec4(+s,+s,-sideStep,0));
    mvAndEmit(gp, vec4(+s,-s,-sideStep,0));
    mvAndEmit(gp, vec4(-s,+s,-sideStep,0));
    mvAndEmit(gp, vec4(-s,-s,-sideStep,0));
    EndPrimitive();
*/

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