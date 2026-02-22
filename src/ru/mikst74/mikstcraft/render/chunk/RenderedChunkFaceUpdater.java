package ru.mikst74.mikstcraft.render.chunk;

import ru.mikst74.mikstcraft.render.RenderedWorldArea;
import ru.mikst74.mikstcraft.render.buffers.PerFaceBuffers;
import ru.mikst74.mikstcraft.util.array.DynamicByteBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static ru.mikst74.mikstcraft.model.NeighborCode.forEachNeighborCode;
import static ru.mikst74.mikstcraft.render.buffers.PerFaceBuffers.deallocatePerFaceBufferRegion;
import static ru.mikst74.mikstcraft.settings.GameProperties.DEBUG2;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL31C.GL_TEXTURE_BUFFER;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * Created by Mikhail Krinitsyn on 10.01.2026
 */
public class RenderedChunkFaceUpdater {
    RenderedWorldArea renderedWorldArea;

    public RenderedChunkFaceUpdater(RenderedWorldArea renderedWorldArea) {
        this.renderedWorldArea = renderedWorldArea;
    }

    /**
     * Update the chunk's per-face buffer region with the given vertex and index data.
     *
     * @param chunk
     * @param vertexData
     * @param indices
     */
    public void updateChunkVertexAndIndexDataInBufferObjects(RenderedChunk chunk, DynamicByteBuffer[] vertexData,
                                                             DynamicByteBuffer[] indices) {
        forEachNeighborCode((nc) -> {
            long vertexOffset = (long) chunk.r[nc.getI()].off * PerFaceBuffers.voxelVertexSize * PerFaceBuffers.verticesPerFace;
            glBindBuffer(GL_ARRAY_BUFFER, PerFaceBuffers.vertexDataBufferObject);
            nglBufferSubData(GL_ARRAY_BUFFER, vertexOffset, vertexData[nc.getI()].pos, vertexData[nc.getI()].addr);
            updateChunkInfo(chunk);

            long indexOffset = (long) chunk.r[nc.getI()].off * Short.BYTES * PerFaceBuffers.indicesPerFace;
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, PerFaceBuffers.indexBufferObject);
            nglBufferSubData(GL_ELEMENT_ARRAY_BUFFER, indexOffset, indices[nc.getI()].pos, indices[nc.getI()].addr);
//        if (GameProperties.DEBUG) {
//            System.out.println("Number of chunks: " + GameProperties.INT_FORMATTER.format(chunkHolder.count()) + " ("
//                    + GameProperties.INT_FORMATTER.format(chunkHolder.computePerFaceBufferObjectSize() / 1024 / 1024) + " MB)");
//            System.out.println("Number of faces:  " + GameProperties.INT_FORMATTER.format(chunkHolder.activeFaceCount));
//        }
            renderedWorldArea.getChunkBuildTasksCount().decrementAndGet();
        });
        chunk.ready = true;
    }

    /**
     * Update the chunk info buffer with the (new) minY/maxY of the chunk, or write the initial data for
     * a newly created chunk.
     *
     * @param renderedChunk
     */
    private void updateChunkInfo(RenderedChunk renderedChunk) {
        try (MemoryStack stack = stackPush()) {
            int bindTarget =  GL_TEXTURE_BUFFER;
            glBindBuffer(bindTarget, PerFaceBuffers.chunkInfoBufferObject);
            IntBuffer data = stack.mallocInt(4);
            // chunk.vs.glsl
            //      uniform isamplerBuffer chunkInfo;
            //  Содержит координаты чанка и индекс чанка (4 * int)
            //       ci = texelFetch(chunkInfo, int(chunkIndex)); где
            //
            data
                    .put(0, renderedChunk.getWCoo().getX())
                    .put(1, renderedChunk.getWCoo().getY())
                    .put(2, renderedChunk.getWCoo().getZ())
                    .put(3, renderedChunk.index);
            glBufferSubData(bindTarget, (long) renderedChunk.index * 4 * Integer.BYTES, data);
        }
    }

    /**
     * Update the chunk's buffer objects with the given voxel field.
     *
     * @param renderedChunk
     */
    public void updateChunk(RenderedChunk renderedChunk) {
        if (DEBUG2) {
            System.out.println("updateChunk: " + renderedChunk);
        }
        if (renderedChunk.lastMeshUpdateTime > 0) {
            forEachNeighborCode((nc) -> renderedWorldArea.activeFaceCount -= renderedChunk.r[nc.getI()].len);
            deallocatePerFaceBufferRegion(renderedChunk);
        }
        renderedWorldArea.getRenderedChunkMesher().meshChunkFacesAndWriteToBuffers(renderedChunk);
    }



}
