package ru.mikst74.mikstcraft.render.chunk;

import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import ru.mikst74.mikstcraft.input.InputEventData;
import ru.mikst74.mikstcraft.render.buffers.PerFaceBuffers;
import ru.mikst74.mikstcraft.render.shader.chunk.ChunkShaderProgram;

import java.nio.IntBuffer;

import static ru.mikst74.mikstcraft.util.time.Profiler.profile;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL31C.GL_TEXTURE_BUFFER;
import static org.lwjgl.opengl.GL32C.glMultiDrawElementsBaseVertex;

@Getter
public class ChunkRenderer {
    private final ChunkShaderProgram shaderProgram;
    @Setter
    private       Matrix4f           mvp;
    private       Vector4f           tmvp;
    @Setter
    private       Vector3f           position;
    private       boolean            wireframe;

    public ChunkRenderer() {
        this.shaderProgram = new ChunkShaderProgram();
        shaderProgram.init();
        this.mvp      = new Matrix4f();
        this.tmvp     = new Vector4f();
        this.position = new Vector3f();
    }

    public ChunkRenderer withMvp(Matrix4f mvp) {
        this.mvp = mvp;
        return this;
    }

    public ChunkRenderer withPos(Vector3f pos) {
        this.position = pos;
        return this;
    }

    public ChunkRenderer initialize() {
        shaderProgram.activateShader();
        shaderProgram.updateUbo(0, mvp, position);
        shaderProgram.deactivateShader();

        return this;
    }


    /**
     * Setup GL state prior to drawing the chunks.
     *
     */
    public void preDrawChunksState() {
        if (wireframe) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            glDisable(GL_CULL_FACE);
        } else {
            glEnable(GL_CULL_FACE);
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_BUFFER, PerFaceBuffers.chunkInfoTexture);

        glBindVertexArray(PerFaceBuffers.chunksVao);
//        glUseProgram(VoxelGame2GL.chunksProgram);
        /*
         * Bind the UBO holding camera matrices for drawing the chunks.
         */
//        int uboSize = roundUpToNextMultiple(VoxelGame2GL.chunksProgramUboSize, uniformBufferOffsetAlignment);
//        glBindBufferRange(GL_UNIFORM_BUFFER, VoxelGame2GL.chunksProgramUboBlockIndex, VoxelGame2GL.chunksProgramUbo, (long) currentDynamicBufferIndex * uboSize, uboSize);

        /*
         * Bind the atomic counter buffer to the atomic counter buffer binding point. We will use
         * glGetBufferSubData() to actually read-back the value of the counter from the buffer.
         */
//        glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, VoxelGame2GL.atomicCounterBuffer);

        /*
         * Bind the indirect buffer containing the final MDI draw structs for all chunks that are visible.
         */
//        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, VoxelGame2GL.indirectDrawCulledBuffer);
    }


    /**
     * Update the current region of the UBO for the voxels program.
     *
     */
//    public void updateChunksProgramUbo() {
//        int size = roundUpToNextMultiple(VoxelGame2GL.chunksProgramUboSize, uniformBufferOffsetAlignment);
//        try (MemoryStack stack = stackPush()) {
//            long ubo, uboPos;
//
//            // when cause "@"java.lang.OutOfMemoryError: Out of stack space."
//            // it is mean that too small Configuration.STACK_SIZE - add jvm start argument "-Dorg.lwjgl.system.stackSize=1024" (size in kb, default 64)
//            ubo    = stack.nmalloc(size);
//            uboPos = 0L;
//
//            mvp.getToAddress(ubo + uboPos);
//            uboPos += 16 * Float.BYTES;
////            VoxelGame2GL.mvpMat.getRow(3, VoxelGame2GL.tmpv4f).getToAddress(ubo + uboPos);
////            uboPos += 4 * Float.BYTES;
////            memPutInt(ubo + uboPos, (int) floor(player.getPosition().x));
////            memPutInt(ubo + uboPos + Integer.BYTES, (int) floor(player.getPosition().y));
////            memPutInt(ubo + uboPos + Integer.BYTES * 2, (int) floor(player.getPosition().z));
//            position.getToAddress(ubo+uboPos);
//            uboPos += 3 * Float.BYTES;
//            glBindBuffer(GL_UNIFORM_BUFFER, VoxelGame2GL.chunksProgramUbo);
//
//            nglBufferSubData(GL_UNIFORM_BUFFER, (long) currentDynamicBufferIndex * size, uboPos, ubo);
//
//        }
//    }
    public void render(IntBuffer count, PointerBuffer indices, IntBuffer basevertex) {
        preDrawChunksState();
        shaderProgram.activateShader();
        shaderProgram.updateUbo(0, mvp, position);
        glBindVertexArray(PerFaceBuffers.chunksVao);

        profile("glMultiDrawElementsBaseVertex", () -> {
            glMultiDrawElementsBaseVertex(GL_TRIANGLE_STRIP, count, GL_UNSIGNED_SHORT, indices, basevertex);
        });
//        System.out.println("draw +"+count+" points");
        shaderProgram.deactivateShader();
    }


    public void wireFrameToggler(InputEventData inputEventData) {
        wireframe = !wireframe;
    }

}
