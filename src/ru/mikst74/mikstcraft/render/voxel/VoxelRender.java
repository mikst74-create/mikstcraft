package ru.mikst74.mikstcraft.render.voxel;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector4f;
import ru.mikst74.mikstcraft.settings.GameProperties;
import ru.mikst74.mikstcraft.util.time.Profiler;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.floor;
import static java.lang.Math.sin;
import static ru.mikst74.mikstcraft.VoxelGame2GL.nullVao;
import static ru.mikst74.mikstcraft.render.opengl.ShaderCreator.createShader;
import static ru.mikst74.mikstcraft.settings.OpenGLProperties.currentDynamicBufferIndex;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.glBindBufferRange;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL31C.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL32C.GL_GEOMETRY_SHADER;
import static org.lwjgl.system.MemoryStack.stackPush;

public class VoxelRender {
    private int programUboBlockIndex;
    private int programId;
    private int programUbo;
    private int programUboSize = 4 * 16 + 4 * 4;
    private final Matrix4f tmpMat = new Matrix4f();
    private final Vector4f timeVector = new Vector4f();

    public VoxelRender() {
        createProgram();
        createProgramUbo();
    }

    public void createProgram() {
        Map<String, String> defines = new HashMap<>();
        int program = glCreateProgram();
        int vshader = createShader("org/lwjgl/demo/game2/voxelgame/shader/voxel/voxel.vs.glsl", GL_VERTEX_SHADER, defines);
        int gshader = createShader("org/lwjgl/demo/game2/voxelgame/shader/voxel/voxel.gs.glsl", GL_GEOMETRY_SHADER, defines);
        int fshader = createShader("org/lwjgl/demo/game2/voxelgame/shader/voxel/voxel.fs.glsl", GL_FRAGMENT_SHADER, defines);
        glAttachShader(program, vshader);
        glAttachShader(program, gshader);
        glAttachShader(program, fshader);
        glLinkProgram(program);
        glDeleteShader(vshader);
        glDeleteShader(gshader);
        glDeleteShader(fshader);
        if (GameProperties.DEBUG || GameProperties.GLDEBUG) {
            int linked = glGetProgrami(program, GL_LINK_STATUS);
            String programLog = glGetProgramInfoLog(program);
            if (programLog.trim().length() > 0) {
                System.err.println(programLog);
            }
            if (linked == 0) {
                throw new AssertionError("Could not link program");
            }
        }
        glUseProgram(program);
        programUboBlockIndex = glGetUniformBlockIndex(program, "Uniforms");

        glUseProgram(0);
        programId = program;
    }

    public void createProgramUbo() {
        programUbo = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, programUbo);
        int size = programUboSize;

        glBufferData(GL_UNIFORM_BUFFER, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * size, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }

    public void updateProgramUbo(Matrix4f tmpMat) {
        /* Round up to the next multiple of the UBO alignment */
        int size = programUboSize;
        try (MemoryStack stack = stackPush()) {
            long ubo, uboPos;

            ubo = stack.nmalloc(size);
            uboPos = 0L;

            tmpMat.getToAddress(ubo + uboPos);
            uboPos += 16 * Float.BYTES;

            timeVector.x = 0.0000001f * (System.nanoTime() - Profiler.start);
            timeVector.getToAddress(ubo + uboPos);
            uboPos += 4 * Float.BYTES;
            glBindBufferRange(GL_UNIFORM_BUFFER, programUboBlockIndex, programUbo, (long) currentDynamicBufferIndex * size, size);

            nglBufferSubData(GL_UNIFORM_BUFFER, (long) currentDynamicBufferIndex * size, uboPos, ubo);

        }
    }

    public void draw(Vector3d center, Matrix4f mvpMat) {
//        glDisable(GL_CULL_FACE);
        glEnable(GL_CULL_FACE);
//        glDisable(GL_DEPTH_TEST);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
//        glDisable(GL_BLEND);
//        glEnable(GL_POLYGON_OFFSET_FILL);
//        glPolygonOffset(useInverseDepth ? 1 : -1, useInverseDepth ? 1 : -1);
//        glEnable(GL_POLYGON_OFFSET_FILL);
//        glPolygonOffset(1.0f, 1.0f);
//        glEnable(GL_MULTISAMPLE);
        glBindVertexArray(nullVao);
        glUseProgram(programId);

        /* compute a player-relative position. The MVP matrix is already player-centered */
        double dx = /*selectedVoxelPosition.x */ 0 - floor(center.x);
        double dy = /*selectedVoxelPosition.y */ 0 - floor(center.y);
        double dz = /*selectedVoxelPosition.z */ 0 - floor(center.z);
        tmpMat.set(mvpMat).translate((float) dx, (float) dy, (float) dz);
        /* translate and rotate based on face side */
//        if (sideOffset.x != 0) {
//            tmpMat.translate(sideOffset.x > 0 ? 1 : 0, 0, 1).mul3x3(0, 0, -1, 0, 1, 0, 1, 0, 0);
//        } else if (sideOffset.y != 0) {
//            tmpMat.translate(0, sideOffset.y > 0 ? 1 : 0, 1).mul3x3(1, 0, 0, 0, 0, -1, 0, 1, 0);
//        } else if (sideOffset.z != 0) {
//            tmpMat.translate(0, 0, sideOffset.z > 0 ? 1 : 0).mul3x3(1, 0, 0, 0, 1, 0, 0, 0, 1);
//        }
        /* animate it a bit */
        float s = (float) sin((double) System.currentTimeMillis() / 567);
        // Move matrix to center voxel and zoom for animate
        tmpMat.translate(0.5f, 0.5f, 0.5f);//.scale(0.3f + 0.1f * s * s);
        updateProgramUbo(tmpMat);
//        glDrawArrays(GL_TRIANGLE_STRIP, 0, 1);
        glDrawArrays(GL_POINTS, 0, 16 * 16 * 24);
//        glDisable(GL_POLYGON_OFFSET_FILL);
//        glPolygonOffset(0, 0);
    }
}
