package ru.mikst74.mikstcraft.render.shader.selectedvoxel;

import lombok.Getter;
import ru.mikst74.mikstcraft.render.shader.common.ShaderProgram;

import static org.lwjgl.opengl.GL15C.*;

@Getter
public class SelectedVoxelShaderProgram extends ShaderProgram {

    public void init() {
        createProgram();
        addVertexShader("selectedvoxel/selectedvoxel.vs.glsl");
        addFragmentShader("selectedvoxel/selectedvoxel.fs.glsl");
        linkProgram();
        addStd140Uniform("Uniforms", 80, false);
        addNullVao();
        finish();
    }


    public void render() {
        glDisable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glEnable(GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(-1, -1);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        glDisable(GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(0, 0);
    }
}
