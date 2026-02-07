#version 330 core

out vec4 color;
in FS_IN {
    vec3 color;
    vec2 surfacePos;
} fs_in;

const float gridThickness = 0.03;

/*
 * Adapted from: https://www.shadertoy.com/view/wl3Sz2
 */


float filterWidth2(vec2 uv) {
    vec2 dx = dFdx(uv), dy = dFdy(uv);
    return max(length(dx), length(dy));
}
float gridSmooth() {
    vec2 q = fs_in.surfacePos + vec2(0.5);
    q -= floor(q);
    q = (gridThickness + 1.0) * 0.5 - abs(q - 0.5);
    float w = 5.0 * filterWidth2(fs_in.surfacePos);
    return   smoothstep(0.5 - w * sqrt(gridThickness), 0.5 + w, max(q.x, q.y));
}

void main()
{
    float gs = gridSmooth();
    color = vec4(fs_in.color, gs);
}