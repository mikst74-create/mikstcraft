package ru.mikst74.mikstcraft.render.chunk;

import ru.mikst74.mikstcraft.model.NeighborCode;
import ru.mikst74.mikstcraft.render.RenderedWorldArea;
import ru.mikst74.mikstcraft.settings.GameProperties;
import ru.mikst74.mikstcraft.world.chunk.ChunkFrontierSupplier;

import static ru.mikst74.mikstcraft.render.buffers.PerFaceBuffers.deallocatePerFaceBufferRegion;

/**
 * Created by Mikhail Krinitsyn on 10.01.2026
 */
public class RenderedChunkDestroyer {
    RenderedWorldArea renderedWorldArea;

    public RenderedChunkDestroyer(RenderedWorldArea renderedWorldArea) {
        this.renderedWorldArea = renderedWorldArea;
    }

    /**
     * Destroy the given chunk.
     * <p>
     * Steps to do:
     * <ol>
     * <li>Remove that chunk from the frontier (it likely was a frontier chunk)
     * <li>Promote chunks around it to frontier
     * <li>Deallocate the per-face and per-chunk buffer regions
     * <li>Remove it from the linear list of all chunks
     * <li>Decrement the "active faces" counter
     * <li>Remove it from the coordinate lookup map
     * </ol>
     *
     * @param rc
     */
    public int destroyChunk(RenderedChunk rc) {

        if (GameProperties.DEBUG) {
            System.out.println("Destroying chunk: " + rc);
        }
//        int numDiff = ChunkFrontierSupplier.onFrontierChunkRemoved(renderedChunk.getChunk()) - 1;
        ChunkFrontierSupplier.remove(rc.getChunk());
        if (GameProperties.DEBUG) {
            System.out.println("Removed frontier chunk #" + renderedWorldArea.getFrontierChunks().size() + ": " + rc);
        }
        deallocatePerFaceBufferRegion(rc);
        deallocatePerChunkIndex(rc);
//        chunkHolder.remove(renderedChunk);
        NeighborCode.forEachNeighborCode((nc) -> renderedWorldArea.activeFaceCount -= rc.r[nc.getI()].len);
//        if (GameProperties.DEBUG) {
//            System.out.println("Number of chunks: " + GameProperties.INT_FORMATTER.format(chunkHolder.count()) + " ("
//                    + GameProperties.INT_FORMATTER.format(chunkHolder.computePerFaceBufferObjectSize() / 1024 / 1024) + " MB)");
//            System.out.println("Number of faces:  " + GameProperties.INT_FORMATTER.format(chunkHolder.activeFaceCount));
//        }
//        renderedWorldArea.getChunkByCoordinateMap().remove(rc.cx, rc.cz);

        renderedWorldArea.getMapByCoo().remove(rc.getChunk().getCoo());
        renderedWorldArea.getAllRenderedChunks().remove(rc);
        renderedWorldArea.getFrontierChunks().remove(rc);

        return 0;//numDiff;
    }

    /**
     * Deallocate/free a chunk index for per-chunk buffers/arrays.
     *
     * @param chunk
     */
    private static void deallocatePerChunkIndex(RenderedChunk chunk) {
        /*
         * If we use temporal coherence occlusion culling, we must delay deallocating the chunk index by 1
         * frame, because the next frame still wants to potentially draw the chunk when it was visible last
         * frame, so we must not allocate this index to another chunk immediately.
         */
//        int delayFrames = useOcclusionCulling && useTemporalCoherenceOcclusionCulling ? 1 : 0;
//        updateAndRenderRunnables.add(new DelayedRunnable(() -> {
//            chunkHolder.getChunkIndexes().set(chunk.index, false);
//            if (GameProperties.DEBUG) {
//                System.out.println("Deallocated per-chunk index for chunk: " + chunk);
//            }
//            return null;
//        }, "Deallocate per-chunk index for chunk [" + chunk + "]", delayFrames));
    }
}
