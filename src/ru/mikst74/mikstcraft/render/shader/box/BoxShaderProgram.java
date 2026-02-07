package ru.mikst74.mikstcraft.render.shader.box;

import lombok.Getter;
import org.joml.Vector2i;
import ru.mikst74.mikstcraft.render.shader.common.ShaderProgram;
import ru.mikst74.mikstcraft.util.math.Hitbox;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import static ru.mikst74.mikstcraft.VoxelGame2GL.rgb;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

@Getter
public class BoxShaderProgram extends ShaderProgram {
    protected int        allocatedBoxCount;
    protected int        actualBoxCount;
    protected ByteBuffer vboData;

    public BoxShaderProgram(int allocatedBoxCount) {
        this.allocatedBoxCount = allocatedBoxCount;
    }


    public void init() {
        createProgram();
        addVertexShader("box/box.vs.glsl");
        addGeometryShader("box/box.gs.glsl");
        addFragmentShader("box/box.fs.glsl");
        linkProgram();
        addStd140Uniform("Uniforms", Float.BYTES * (16 + 4), true);
        addVaoVbo();
        finish();
    }


    public void loadVbo(List<Hitbox> items) {
        actualBoxCount = items.size();
        int[] locationBufferPosition = new int[3];
        Integer color = rgb(0, 0, 255);
        if (actualBoxCount > allocatedBoxCount) {
            System.out.println("WARNING: TexturedQuadsShaderProgram.loadVbo: try to load more items (" + actualBoxCount + ") than buffer was allocated (" + allocatedBoxCount + ").");
        }
        vboData.position(0);
        vboData.limit(vboData.capacity());

//        layout (location = 0) in vec4 in_position;// <vec2 pos, vec2 tex>
//        layout (location = 1) in vec4 in_size;// <vec2 pos, vec2 tex>
//        layout (location = 2) in uint in_packedColor;// <vec2 pos, vec2 tex>

        locationBufferPosition[0] = vboData.position();
        items.forEach((quadItem) -> {
            quadItem.getActual().get(vboData);
            vboData.position(vboData.position() + 12); // 12 - 3 floats of 4 bytes each
        });

        locationBufferPosition[1] = vboData.position();
        items.forEach((quadItem) -> {
            quadItem.getSize().get(vboData);
            vboData.position(vboData.position() + 12);
        });

        locationBufferPosition[2] = vboData.position();
        items.forEach((quadItem) -> {
            vboData.putInt(color);
            vboData.position(vboData.position() + 4);
        });
        vboData.flip();
        glBindVertexArray(getVao());
        glBindBuffer(GL_ARRAY_BUFFER, getVbo());

        glBufferData(GL_ARRAY_BUFFER, vboData, GL_DYNAMIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0 /* location=0*/, 4, GL_FLOAT, false, 0, locationBufferPosition[0]);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1/* location=1*/, 4, GL_FLOAT, false, 0, locationBufferPosition[1]);//смещение от начала буфера в байтах
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2/* location=2*/, 1, GL_INT, false, 0, locationBufferPosition[2]);//смещение от начала буфера в байтах
    }

    // Создает Vao объект
    public Vector2i createVaoVbo() {

        // Если vao уже создан, не создавать повторно
        int localVao = glGenVertexArrays();

        glBindVertexArray(localVao);

        // create VBO
        int localVbo = glGenBuffers();
        int vboSizeBytes = allocatedBoxCount * (4 * 4 + 4 * 4 + 4);//sizeOf(new Vector4f()) * 3 * allocatedBoxCount; // один элемент состоит из двух Vector4f
        vboData = ByteBuffer.allocateDirect(vboSizeBytes);
        vboData.order(ByteOrder.nativeOrder());
//        vboData0 = byteBuffer.asFloatBuffer(); //memAllocFloat(2 * quadCount);

        glBindBuffer(GL_ARRAY_BUFFER, localVbo);

        glBufferData(GL_ARRAY_BUFFER, vboSizeBytes, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glBindVertexArray(0);

        return new Vector2i(localVao, localVbo);
    }

    public void render() {
        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glEnable(GL_POLYGON_OFFSET_FILL);
        glEnable(GL_DEPTH_TEST);

        glDrawArrays(GL_POINTS, 0, 1);
    }
}
