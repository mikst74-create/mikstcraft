#version 330 core
layout (location = 0) in vec3 aPos;
out vec3 TexCoords;
uniform mat4 projection;
uniform mat4 view;

void main() {
    TexCoords = aPos;
    // Remove translation by casting to mat3 and back to mat4
    vec4 pos = projection * mat4(mat3(view)) * vec4(aPos, 1.0);
    // Optimization: force the sky to the far plane (z = 1.0)
    gl_Position = pos.xyww;
}