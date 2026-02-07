package ru.mikst74.mikstcraft.render.chunk;

import ru.mikst74.mikstcraft.model.chunk.Chunk;
import ru.mikst74.mikstcraft.render.RenderedWorldArea;
import ru.mikst74.mikstcraft.settings.GameProperties;

public class RenderedChunkCreator {
    RenderedWorldArea renderedWorldArea;

    public RenderedChunkCreator(RenderedWorldArea renderedWorldArea) {
        this.renderedWorldArea = renderedWorldArea;
    }

    /**
     * Allocate a new chunk index for per-chunk buffers/arrays.
     */
    private int allocateChunkIndex() {
        int next = renderedWorldArea.getChunkIndexes().nextClearBit(0);
        if (next >= GameProperties.MAX_ACTIVE_CHUNKS) {
            throw new AssertionError("Failed to allocate per-chunk index");
        }
        if (GameProperties.DEBUG) {
            System.out.println("Allocated pre-chunk index " + next);
        }
        renderedWorldArea.getChunkIndexes().set(next);
        return next;
    }

    public RenderedChunk create(Chunk chunk) {
        RenderedChunk renderedChunk = new RenderedChunk(chunk);
        renderedChunk.index = allocateChunkIndex();
//        NeighborCode.forEachNeighborCode(nc -> {
//            RenderedChunk nrc = renderedWorldArea.getMapByCoo().get(renderedChunk.getChunk().getCoo().getNB(nc));
//            if (nrc != null) {
//                renderedChunk.getNeighborChunks()[nc.getI()]     = nrc;
//                nrc.getNeighborChunks()[nc.getOpposite().getI()] = renderedChunk;
//            }
//        });
        return renderedChunk;
    }
}
