package ru.mikst74.mikstcraft.render.shader.font;

import lombok.Getter;
import org.joml.Vector2i;
import ru.mikst74.mikstcraft.model.font.Font;
import ru.mikst74.mikstcraft.render.shader.common.ShaderProgram;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30C.*;

@Getter
public class FontShaderProgram extends ShaderProgram {
    private final Font font;


    public FontShaderProgram(Font font) {
        this.font = font;
    }

    @Override
    public void init() {
        createProgram();
        addVertexShader("text/text.vs.glsl");
        addGeometryShader("text/text.gs.glsl");
        addFragmentShader("text/text.fs.glsl");
        linkProgram();
        addStd140Uniform("Uniforms", Float.BYTES * (16 + 4 + 4), false);
        addUniformTextureLocation("fontTex", font.getTextureInfo());
        addVaoVbo();
        finish();
    }

    public Vector2i createVaoVbo() {
        int vao = glGenVertexArrays();
        glBindVertexArray(vao);
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        // 2. Define Grid Data (e.g., 10x10 grid lines)
        // For simplicity, create vertices for horizontal lines
//        int pointCount = 20;
        int[] vertices = new int[26 * 4]; // 10 points, 3 coords/point
        int index = 0;
        for (byte l = 0; l < 4; l++) {
            for (byte i = 0; i < 26; i++) {
                // Horizontal line
                vertices[index++] = i | (l << 8);
            }
        }

        // 3. Upload data to VBO
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 1, GL_INT, false, /*3 * Float.BYTES*/0, 0);

        // unbind all
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return new Vector2i(vao, vbo);
    }

//    public void updateProgramUbo(Matrix4f mvp) {
//
//        /* Round up to the next multiple of the UBO alignment */
//        int size = getUboList().get(0).getSize();
//        try (MemoryStack stack = stackPush()) {
//            long ubo, uboPos;
//            ubo = stack.nmalloc(size);
//            uboPos = 0L;
//
//            mvp.getToAddress(ubo + uboPos);
//            uboPos += 16 * Float.BYTES;
//
//            Vector4f grid = new Vector4f(GameDynamicProperties.guiGridHalfSizeGlView, 0, GameDynamicProperties.guiGridHalfSizeInScreenPixel);
//            grid.getToAddress(ubo + uboPos);
//            uboPos += 4 * Float.BYTES;
//
////            glBindBufferRange(GL_UNIFORM_BUFFER, getUniformUboIndex(), getUbo(), (long) currentDynamicBufferIndex * size, size);
//
//            nglBufferSubData(GL_UNIFORM_BUFFER, (long) currentDynamicBufferIndex * size, uboPos, ubo);
//
//        }
//    }


    @Override
    public void render() {

    }
}
