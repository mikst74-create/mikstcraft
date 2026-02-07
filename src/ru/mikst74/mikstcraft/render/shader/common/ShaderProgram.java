package ru.mikst74.mikstcraft.render.shader.common;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2i;
import ru.mikst74.mikstcraft.settings.GameProperties;
import ru.mikst74.mikstcraft.texture.TextureInfo;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.system.MemoryStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.mikst74.mikstcraft.render.opengl.ShaderCreator.createShader;
import static ru.mikst74.mikstcraft.settings.OpenGLProperties.currentDynamicBufferIndex;
import static ru.mikst74.mikstcraft.settings.OpenGLProperties.uniformBufferOffsetAlignment;
import static ru.mikst74.mikstcraft.util.math.ExtMath.roundUpToNextMultiple;
import static ru.mikst74.mikstcraft.util.JomlMemUtilExtender.getToAddress;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL31C.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL32C.GL_GEOMETRY_SHADER;
import static org.lwjgl.system.MemoryStack.stackPush;

@Getter
@Setter
public abstract class ShaderProgram {
    private static final String FILE_BASE_PATH = "shader/";
    private static       int    nullVao        = 0;

    private       int                       id                  = 0;
    private       List<Integer>             shaderIds           = new ArrayList<>();
    private       List<BufferInfo>          uniformStd140List   = new ArrayList<>();
    private final AtomicInteger             unitIdCounter       = new AtomicInteger(0);
    private       List<UniformLocationInfo> uniformLocationList = new ArrayList<>();
    private       List<UniformTextureInfo>  uniformTextureList  = new ArrayList<>();

    private int vao = 0;
    private int vbo = 0;
//    private List<BufferInfo>          uboList;
//    private List<UniformLocationInfo> uLocList;
//    private List<UniformTextureInfo>  textures;

    public ShaderProgram() {
    }

    protected static int getNullVao() {
        return nullVao;
    }

    public abstract void init();

    public void createProgram() {
        id = glCreateProgram();
    }

    public Map<String, String> getDefines() {
        return new HashMap<>();
    }

    public void addVertexShader(String fileName) {
        int shaderId = createShader(FILE_BASE_PATH + fileName, GL_VERTEX_SHADER, getDefines());
        glAttachShader(id, shaderId);
        shaderIds.add(shaderId);
    }

    public void addFragmentShader(String fileName) {
        int shaderId = createShader(FILE_BASE_PATH + fileName, GL_FRAGMENT_SHADER, getDefines());
        glAttachShader(id, shaderId);
        shaderIds.add(shaderId);
    }

    public void addGeometryShader(String fileName) {
        int shaderId = createShader(FILE_BASE_PATH + fileName, GL_GEOMETRY_SHADER, getDefines());
        glAttachShader(id, shaderId);
        shaderIds.add(shaderId);
    }


    public void linkProgram() {
        glLinkProgram(id);
        shaderIds.forEach(GL20C::glDeleteShader);

        if (GameProperties.DEBUG || GameProperties.GLDEBUG) {
            int linked = glGetProgrami(id, GL_LINK_STATUS);
            String programLog = glGetProgramInfoLog(id);
            if (!programLog.trim().isEmpty()) {
                System.err.println(programLog);
            }
            if (linked == 0) {
                throw new AssertionError("Could not link program");
            }
        }

        glUseProgram(id);
    }

    public void addStd140Uniform(String uniformName, int size, boolean isDynamic) {
        int uId = glGetUniformBlockIndex(id, uniformName);
        int roundedSize = roundUpToNextMultiple(size, uniformBufferOffsetAlignment);
        int flags = isDynamic ? BufferInfo.BI_DYNAMIC : BufferInfo.BI_STATIC;

        int uBoId = glGenBuffers();

        glBindBuffer(GL_UNIFORM_BUFFER, uBoId);
        if (isDynamic) {
            glBufferData(GL_UNIFORM_BUFFER, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * roundedSize, GL_DYNAMIC_DRAW);
        } else {
            glBufferData(GL_UNIFORM_BUFFER, (long) roundedSize, GL_STATIC_DRAW);
        }
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        uniformStd140List.add(new BufferInfo(uId, uBoId, roundedSize, flags));
    }

    public void addUniformLocation(String uniformName, int size) {

        int uLocId = glGetUniformLocation(id, uniformName);
        int unitId = unitIdCounter.getAndIncrement();
        glUniform1i(uLocId, unitId);

        uniformLocationList.add(new UniformLocationInfo(uLocId, unitId));
    }

    public void addUniformTextureLocation(String uniformName, TextureInfo textureInfo) {
        int unitId = unitIdCounter.getAndIncrement();
        int texLoc = glGetUniformLocation(id, "chunkTex");

        glActiveTexture(GL_TEXTURE0 + unitId);
        int textureType;
        if (textureInfo.isArray()) {
            textureType = GL_TEXTURE_2D_ARRAY;
        } else {
            textureType = GL_TEXTURE_2D;
        }
        glBindTexture(textureType, textureInfo.getTextureId());
        glUniform1i(texLoc, unitId);

        uniformTextureList.add(new UniformTextureInfo(texLoc, unitId, textureType, textureInfo));

    }

    public void addNullVao() {
        vao = getOrCreateNullVao();
    }

    public void addVaoVbo() {
        Vector2i vv = createVaoVbo();
        vao = vv.x;
        vbo = vv.y;
    }

    public Vector2i createVaoVbo() {
        return new Vector2i(0, 0);
    }


    public void finish() {
        glUseProgram(0);
    }

    private static int getOrCreateNullVao() {
        if (nullVao == 0) {
            nullVao = glGenVertexArrays();
        }
        return nullVao;
    }

    public void activateShader() {
        // Связать с программой VAO и VBO.
        bindVaoVbo();
        // Активировать программу шейдера
        activateProgram();
        activateUbo();

        // Связать все текстуры шейдера с его юнитами
        activateTextures();

    }

    private void activateUbo() {
        uniformStd140List.forEach((uboInfo) ->
                glBindBufferRange(
                        GL_UNIFORM_BUFFER,
                        uboInfo.getId(),
                        uboInfo.getBo(),
                        (long) currentDynamicBufferIndex * uboInfo.getSize(),
                        uboInfo.getSize()));
    }


    public void activateProgram() {
        glUseProgram(id);
    }

    public void deactivateShader() {
        glUseProgram(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        uniformTextureList
                .forEach((v) -> {
                    int unitId = v.getTextureInfo().getTextureId();
                    glActiveTexture(GL_TEXTURE0 + unitId);
                    glBindTexture(v.getTextureType(), 0);
                });
    }

    public void activateTextures() {
        uniformTextureList
                .forEach((v) -> {
                    int texLocation = v.getTextureLocId();
                    int unitId = v.getUnitId();
                    int textureId = v.getTextureInfo().getTextureId();
                    glActiveTexture(GL_TEXTURE0 + unitId);
                    glBindTexture(v.getTextureType(), textureId);
                    glUniform1i(texLocation, unitId);
                });

    }

    public void bindVaoVbo() {
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
    }

    public void updateUbo(int uboIndex, Object... obj) {
        BufferInfo bufferInfo = uniformStd140List.get(uboIndex);
        int size = bufferInfo.getSize();
        try (MemoryStack stack = stackPush()) {
            long startAddress, nextAddress;

            startAddress = stack.nmalloc(size);
            nextAddress  = startAddress;

            for (Object o : obj) {
                nextAddress = getToAddress(o, nextAddress);
            }
            glBindBuffer(GL_UNIFORM_BUFFER, bufferInfo.getId());

            glBindBufferRange(GL_UNIFORM_BUFFER, bufferInfo.getId(), bufferInfo.getBo(), (long) currentDynamicBufferIndex * size, size);

            nglBufferSubData(GL_UNIFORM_BUFFER, (long) currentDynamicBufferIndex * size, nextAddress - startAddress, startAddress);
            glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }
    }

    public void linkTexture(int textureIdx, TextureInfo textureInfo) {
        uniformTextureList.get(textureIdx).setTextureInfo(textureInfo);
    }

    public abstract void render();
}

