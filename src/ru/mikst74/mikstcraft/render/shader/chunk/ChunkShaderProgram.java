package ru.mikst74.mikstcraft.render.shader.chunk;

import lombok.Getter;
import org.joml.Vector2i;
import ru.mikst74.mikstcraft.dictionary.TextureDictionary;
import ru.mikst74.mikstcraft.render.shader.common.ShaderProgram;
import ru.mikst74.mikstcraft.settings.GameProperties;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static ru.mikst74.mikstcraft.dictionary.TextureDictionary.SOLID_BLOCK_TEXTURE_ARRAY;
import static org.lwjgl.opengl.GL15C.GL_POINTS;
import static org.lwjgl.opengl.GL15C.glDrawArrays;

@Getter
public class ChunkShaderProgram extends ShaderProgram {

    public static final Map<String, String> DEFINES = new HashMap<>();
    protected           int                 allocatedQuadCount;
    protected           int                 actualQuadCount;
    protected           FloatBuffer         vboData;

    public ChunkShaderProgram() {
        DEFINES.put("AO_FACTORS", GameProperties.AO_FACTORS);
//        DEFINES.put("MDI","0");
    }

    @Override
    public Map<String, String> getDefines() {
        return DEFINES;
    }

    public void init() {
        createProgram();
        addVertexShader("chunk/chunk.vs.glsl");
        addFragmentShader("chunk/chunk.fs.glsl");
//        addVertexShader("chunk.vs.glsl");
//        addFragmentShader("chunk.fs.glsl");
        linkProgram();
        addStd140Uniform("Uniforms", 88, false);
//        addUniformLocation("XX", 0);
        addUniformLocation("chunkInfo", 0);
        addUniformTextureLocation("chunkTex", TextureDictionary.getTextureInfo(SOLID_BLOCK_TEXTURE_ARRAY));
        addVaoVbo();
        finish();
    }

    public Vector2i createVaoVbo(){
return new Vector2i(0,0);
    }

    public void render() {
        glDrawArrays(GL_POINTS, 0, actualQuadCount);

    }
}
