package ru.mikst74.mikstcraft.render.buffers;

import ru.mikst74.mikstcraft.render.chunk.RenderedChunk;
import ru.mikst74.mikstcraft.settings.GameProperties;
import ru.mikst74.mikstcraft.util.DelayedRunnable;
import ru.mikst74.mikstcraft.util.array.FirstFitFreeListAllocator2;

import static ru.mikst74.mikstcraft.model.NeighborCode.forEachNeighborCode;
import static ru.mikst74.mikstcraft.settings.OpenGLProperties.*;
import static ru.mikst74.mikstcraft.util.BackgroundExecutor.updateAndRenderRunnables;
import static ru.mikst74.mikstcraft.world.chunk.FaceAllocator.allocator;
import static org.lwjgl.opengl.ARBBufferStorage.GL_DYNAMIC_STORAGE_BIT;
import static org.lwjgl.opengl.ARBBufferStorage.glBufferStorage;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL31C.*;

/**
 * Created by Mikhail Krinitsyn on 10.01.2026
 */
public class PerFaceBuffers {
    /* Computed values depending on the render path we use */
    public static int verticesPerFace, indicesPerFace, voxelVertexSize;
    /* Resources for drawing the chunks */
    public static int vertexDataBufferObject;
    public static int indexBufferObject;
    public static int chunkInfoBufferObject;
    public static int chunksVao;
    public static int chunkInfoTexture;

    /**
     * Enlarge the per-face buffer objects and build a VAO for it.
     *
     * @param perFaceBufferCapacity
     * @param newPerFaceBufferCapacity
     */
    public static void enlargePerFaceBuffers(int perFaceBufferCapacity, int newPerFaceBufferCapacity) {
        int vao;
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        int localVertexDataBufferObject;
        long vertexDataBufferSize = (long) voxelVertexSize * verticesPerFace * newPerFaceBufferCapacity;
        /* Create the new vertex buffer */

        localVertexDataBufferObject = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, localVertexDataBufferObject);
        glBufferData(GL_ARRAY_BUFFER, vertexDataBufferSize, GL_STATIC_DRAW);

        /* Setup the vertex specifications */
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glVertexAttribIPointer(0, drawPointsWithGS ? 1 : 4, drawPointsWithGS ? GL_UNSIGNED_INT : GL_UNSIGNED_BYTE, voxelVertexSize, 0L);
        glVertexAttribIPointer(1, drawPointsWithGS ? 4 : 2, GL_UNSIGNED_BYTE, voxelVertexSize, 4L);

        glVertexAttribIPointer(2, 1, GL_UNSIGNED_INT, voxelVertexSize, drawPointsWithGS ? 8L : 6L);

        /* Setup the index buffer */
        long indexBufferSize = (long) Short.BYTES * indicesPerFace * newPerFaceBufferCapacity;
        int localIndexBufferObject;
        localIndexBufferObject = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, localIndexBufferObject);

        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBufferSize, GL_STATIC_DRAW);

        if (chunksVao != 0) {
            if (GameProperties.DEBUG) {
                System.out.println("Copying old buffer objects [" + perFaceBufferCapacity + "] to new");
            }
            /* Copy old buffer objects to new buffer objects */
            glBindBuffer(GL_COPY_READ_BUFFER, localVertexDataBufferObject);
            glBindBuffer(GL_COPY_WRITE_BUFFER, localVertexDataBufferObject);
            glCopyBufferSubData(GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER, 0L, 0L, (long) voxelVertexSize * perFaceBufferCapacity * verticesPerFace);
            glBindBuffer(GL_COPY_READ_BUFFER, localIndexBufferObject);
            glBindBuffer(GL_COPY_WRITE_BUFFER, localIndexBufferObject);
            glCopyBufferSubData(GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER, 0L, 0L, (long) Short.BYTES * perFaceBufferCapacity * indicesPerFace);
            glBindBuffer(GL_COPY_READ_BUFFER, 0);
            glBindBuffer(GL_COPY_WRITE_BUFFER, 0);
            /* Delete old buffers */
            glDeleteBuffers(new int[]{localVertexDataBufferObject, localIndexBufferObject});
            glDeleteVertexArrays(chunksVao);
        }
        /* Remember new vao and buffer objects */
        vertexDataBufferObject = localVertexDataBufferObject;
        indexBufferObject      = localIndexBufferObject;
        chunksVao              = vao;
//        perFaceBufferCapacity = newPerFaceBufferCapacity;
        if (GameProperties.DEBUG) {
            System.out.println("Total size of face buffers: "
                    + GameProperties.INT_FORMATTER.format(newPerFaceBufferCapacity * ((4L + 2) * verticesPerFace + (long) Short.BYTES * indicesPerFace) / 1024 / 1024)
                    + " MB");
        }
    }

    /**
     * When a chunk got destroyed, then this method is called to deallocate that chunk's per-face buffer
     * region.
     * <p>
     * Care must be taken when we use temporal coherence occlusion culling: We do not
     * <em>immediately</em> want to mark the buffer region as free and allocate a new chunk to it when
     * we still want to render the last frame's chunks with the MDI structs already recorded in the
     * indirect draw buffer with their respective offsets/sizes.
     * <p>
     * Instead, we delay marking buffer regions as free for one frame.
     *
     * @param renderedChunk
     */
    public static void deallocatePerFaceBufferRegion(RenderedChunk renderedChunk) {
        forEachNeighborCode((nc) -> {
            int chunkFaceOffset = renderedChunk.r[nc.getI()].off;
            int chunkFaceCount = renderedChunk.r[nc.getI()].len;
            /*
             * If we use temporal coherence occlusion culling, we must delay deallocating the buffer region by 1
             * frame, because the next frame still wants to potentially draw the chunk when it was visible last
             * frame, so we must not allocate this region to another chunk immediately.
             */
            int delayFrames = useOcclusionCulling && useTemporalCoherenceOcclusionCulling ? 1 : 0;
            updateAndRenderRunnables.add(new DelayedRunnable(() -> {
                if (GameProperties.DEBUG) {
                    System.out.println("Deallocate buffer region for chunk: " + renderedChunk);
                }
                allocator.free(new FirstFitFreeListAllocator2.Region(chunkFaceOffset, chunkFaceCount));
                return null;
            }, "Deallocate buffer region for chunk " + renderedChunk, delayFrames));
        });
    }

    /**
     * Allocate a free buffer region with enough space to hold all buffer data for the given number of
     * voxel faces.
     *
     * @param faceCount
     */
    public static FirstFitFreeListAllocator2.Region allocatePerFaceBufferRegion(int faceCount) {
        return allocator.allocate(faceCount);
    }

    /**
     * Create the buffer holding the chunk information (currently the position of the chunk).
     * <p>
     * If we use MDI, then the buffer will be an instanced array buffer, else it will be a texture
     * buffer, where the shader will lookup the chunk info based on chunk index (stored as per-vertex
     * attribute).
     */
    public static void createChunkInfoBuffers() {
        chunkInfoBufferObject = glGenBuffers();
        /*
         * When we have MDI we will use an instanced vertex attribute to hold the chunk position, where each
         * chunk is a separate instance. When we don't have MDI, we will use a buffer texture and lookup in
         * the shader.
         */

        chunkInfoTexture = glGenTextures();
        glBindBuffer(GL_TEXTURE_BUFFER, chunkInfoBufferObject);
        glBindTexture(GL_TEXTURE_BUFFER, chunkInfoTexture);
        if (useBufferStorage) {
            glBufferStorage(GL_TEXTURE_BUFFER, 4 * Integer.BYTES * GameProperties.MAX_ACTIVE_CHUNKS, GL_DYNAMIC_STORAGE_BIT);
        } else {
            glBufferData(GL_TEXTURE_BUFFER, 4 * Integer.BYTES * GameProperties.MAX_ACTIVE_CHUNKS, GL_STATIC_DRAW);
        }
        glTexBuffer(GL_TEXTURE_BUFFER, GL_RGBA32UI, chunkInfoBufferObject);
        glBindTexture(GL_TEXTURE_BUFFER, 0);

    }

}
