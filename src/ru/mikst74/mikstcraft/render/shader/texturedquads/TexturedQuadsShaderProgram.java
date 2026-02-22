package ru.mikst74.mikstcraft.render.shader.texturedquads;

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
public class TexturedQuadsShaderProgram extends ShaderProgram {
    protected int         allocatedQuadCount;
    protected int         actualQuadCount;
    protected FloatBuffer vboData;

    public TexturedQuadsShaderProgram(int allocatedQuadCount) {
        this.allocatedQuadCount = allocatedQuadCount;
    }


    public void init() {
        createProgram();
        addVertexShader("texturedquads/texturedquads.vs.glsl");
        addGeometryShader("texturedquads/texturedquads.gs.glsl");
        addFragmentShader("texturedquads/texturedquads.fs.glsl");
        linkProgram();
        addStd140Uniform("Uniforms", Float.BYTES * (16 + 4), false);
        addUniformTextureLocation("tex", TextureInfo.NO_TEXTURE);
        addVaoVbo();
        finish();
    }

//    public void linkTexture(TextureInfo textureInfo) {
//        getTextures().get(0).setTextureInfo(textureInfo);
//    }

    public void loadVbo(List<TexturedQuadsItem> items) {
        actualQuadCount = items.size();
        if (actualQuadCount > allocatedQuadCount) {
            System.out.println("WARNING: TexturedQuadsShaderProgram.loadVbo: try to load more items (" + actualQuadCount + ") than buffer was allocated (" + allocatedQuadCount + ").");
        }
        vboData.position(0);
        vboData.limit(vboData.capacity());
        items.forEach((quadItem) -> {
            quadItem.getQuadPos().get(vboData);
            vboData.position(vboData.position() + 4);
        });
        items.forEach((quadItem) -> {
            quadItem.getTexPos().get(vboData);
            vboData.position(vboData.position() + 4);
        });
        items.forEach((quadItem) -> {
            quadItem.getColorOverlay().get(vboData);
            vboData.position(vboData.position() + 4);
        });
        vboData.flip();
        glBindVertexArray(getVao());
        glBindBuffer(GL_ARRAY_BUFFER, getVbo());

        glBufferData(GL_ARRAY_BUFFER, vboData, GL_DYNAMIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0 /* location=0*/, 4, GL_FLOAT, false, 0, (long) Float.BYTES * 4 * actualQuadCount * 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1/* location=1*/, 4, GL_FLOAT, false, 0, (long) Float.BYTES * 4 * actualQuadCount * 1);//смещение от начала буфера в байтах
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2/* location=2*/, 4, GL_FLOAT, false, 0, (long) Float.BYTES * 4 * actualQuadCount * 2);//смещение от начала буфера в байтах
    }

    // Создает Vao объект
    public Vector2i createVaoVbo() {

        // Если vao уже создан, не создавать повторно
        int localVao = glGenVertexArrays();

        glBindVertexArray(localVao);

        // create VBO
        // если VBO уже был создан, сначала удалить старый


        int localVbo = glGenBuffers();
        int vboSizeBytes = sizeOf(new Vector4f()) * 3 * allocatedQuadCount; // один элемент состоит из двух Vector4f
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
        glDrawArrays(GL_POINTS, 0, actualQuadCount);
    }
}
