package ru.mikst74.mikstcraft.render.shader.free3dobject;

import lombok.Getter;
import org.joml.Vector2i;
import org.joml.Vector4f;
import ru.mikst74.mikstcraft.render.shader.common.ShaderProgram;
import ru.mikst74.mikstcraft.texture.TextureInfo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import static ru.mikst74.mikstcraft.util.ByteSizeGetter.sizeOf;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

@Getter
public class Free3DObjectShaderProgram extends ShaderProgram {
    protected int         allocatedVertexCount;
    protected int         actualVertexCount;
    protected FloatBuffer vboData;


    public Free3DObjectShaderProgram(int allocatedVertexCount) {
        this.allocatedVertexCount = allocatedVertexCount;
    }


    @Override
    public void init() {
        createProgram();
        addVertexShader("free3dobject/free3dobject.vs.glsl");
        addFragmentShader("free3dobject/free3dobject.fs.glsl");
        linkProgram();
        addStd140Uniform("Uniforms", Float.BYTES * (16 + 4), false);
        addUniformTextureLocation("tex", TextureInfo.NO_TEXTURE);
        addVaoVbo();
        finish();
    }

    public void loadVbo(List<Free3DObjectVertex> items) {
        actualVertexCount = items.size();
        if (actualVertexCount > allocatedVertexCount) {
            System.out.println("WARNING: Free3DObjectShaderProgram.loadVbo: try to load more items (" + actualVertexCount + ") than buffer was allocated (" + allocatedVertexCount + ").");
        }
        vboData.position(0);
        vboData.limit(vboData.capacity());
        items.forEach((vertex) -> {
            vertex.getPos().get(vboData);
            vboData.position(vboData.position() + 4);
        });
        items.forEach((vertex) -> {
            vertex.getTexPos().get(vboData);
            vboData.position(vboData.position() + 4);
        });
        items.forEach((vertex) -> {
            vertex.getColorOverlay().get(vboData);
            vboData.position(vboData.position() + 4);
        });
        vboData.flip();
        glBindVertexArray(getVao());
        glBindBuffer(GL_ARRAY_BUFFER, getVbo());

        glBufferData(GL_ARRAY_BUFFER, vboData, GL_DYNAMIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0 /* location=0*/, 4, GL_FLOAT, false, 0, (long) Float.BYTES * 4 * actualVertexCount * 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1/* location=1*/, 4, GL_FLOAT, false, 0, (long) Float.BYTES * 4 * actualVertexCount * 1);//смещение от начала буфера в байтах
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2/* location=2*/, 4, GL_FLOAT, false, 0, (long) Float.BYTES * 4 * actualVertexCount * 2);//смещение от начала буфера в байтах
    }

    // Создает Vao объект
    public Vector2i createVaoVbo() {

        // Если vao уже создан, не создавать повторно
//        int vao = getVao() == ShaderProgram.getNullVao() ? glGenVertexArrays() : getVao();
        int localVao = glGenVertexArrays();
        glBindVertexArray(localVao);

        // create VBO
        // если VBO уже был создан, сначала удалить старый
//        if (getVbo() != 0) {
//            glBindBuffer(GL_ARRAY_BUFFER, 0); // отвязать vbo от vao
//            glDeleteBuffers(getVbo()); // удалить vbo
//            setVbo(0);
//            memFree(vboData);
//        }

        int localVbo = glGenBuffers();
        int vboSizeBytes = sizeOf(new Vector4f()) * 3 * allocatedVertexCount; // один элемент состоит из трех Vector4f
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vboSizeBytes);
        byteBuffer.order(ByteOrder.nativeOrder());
        vboData = byteBuffer.asFloatBuffer(); //memAllocFloat(2 * quadCount);

        glBindBuffer(GL_ARRAY_BUFFER, localVbo);

        glBufferData(GL_ARRAY_BUFFER, vboSizeBytes, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glBindVertexArray(0);

        return new Vector2i(localVao, localVbo);
    }

    public void render() {
        glDrawArrays(GL_TRIANGLES, 0, actualVertexCount);
    }
}
