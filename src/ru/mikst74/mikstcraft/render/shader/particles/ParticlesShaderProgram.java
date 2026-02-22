package ru.mikst74.mikstcraft.render.shader.particles;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import ru.mikst74.mikstcraft.render.shader.common.ShaderProgram;
import ru.mikst74.mikstcraft.util.time.Profiler;

@Getter
public class ParticlesShaderProgram extends ShaderProgram {

    public ParticlesShaderProgram() {
    }

    public void init(){
        createProgram();
//        addVertexShader("virtual/virtual.vs.glsl");
//        addGeometryShader("virtual/virtual.gs.glsl");
//        addFragmentShader("virtual/virtual.fs.glsl");
        addVertexShader("box/box.vs.glsl");
        addGeometryShader("box/box.gs.glsl");
        addFragmentShader("box/box.fs.glsl");
        linkProgram();
        addStd140Uniform("Uniforms", Float.BYTES * (16 + 4), true);
        addNullVao();
        finish();
    }

    public void updateProgramUbo(Matrix4f mvp, Vector4f timeVector) {
        timeVector.x = 0.0000001f * (System.nanoTime() - Profiler.start);
        
        updateUbo(0,mvp,timeVector);

    }

    @Override
    public void render() {

    }
}
