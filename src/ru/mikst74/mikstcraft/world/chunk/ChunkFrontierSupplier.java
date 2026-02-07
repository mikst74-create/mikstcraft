package ru.mikst74.mikstcraft.world.chunk;

import ru.mikst74.mikstcraft.model.chunk.Chunk;

/**
 * Created by Mikhail Krinitsyn on 10.01.2026
 */
public class ChunkFrontierSupplier {
    private static ChunkManager chunkManager;

    public static void initStatic(ChunkManager chunkManager) {
        ChunkFrontierSupplier.chunkManager = chunkManager;
    }

    /**
     * Called whenever the given chunk (which is automatically made frontier) became visible.
     *
     * @param chunk
     */
    public static void addFrontier(Chunk chunk) {
//        chunkHolder.getFrontierChunks().add(chunk);
//        if (GameProperties.DEBUG) {
//            System.out.println("Added frontier chunk #" + chunkHolder.getFrontierChunks().size() + ": " + chunk);
//        }
//        int cx = chunk.cx, cz = chunk.cz;
        /*
         * Update the four neighbors of this new frontier chunk, because those chunks might not be frontier
         * anymore, once all their four neighbors are occupied.
         */
//        updateFrontierNeighbor(chunk, cx - 1, cz);
//        updateFrontierNeighbor(chunk, cx + 1, cz);
//        updateFrontierNeighbor(chunk, cx, cz - 1);
//        updateFrontierNeighbor(chunk, cx, cz + 1);
    }

    /**
     * Called whenever a new frontier chunk was created, in order to update its neighbor at chunk
     * position <code>(cx, cz)</code>.
     *
     * @param frontier
     * @param cx
     * @param cz
     */
    private static void updateFrontierNeighbor(Chunk frontier, int cx, int cz) {
//        Chunk n = chunkHolder.getChunkByCoordinate(new Vector2i(cx, cz));
//        if (n != null) {
//            n.neighbors++;
//            frontier.neighbors++;
//            if (n.neighbors == 4) {
//                chunkHolder.getFrontierChunks().remove(n);
//                if (GameProperties.DEBUG) {
//                    System.out.println("Removed surrounded frontier chunk #" + chunkHolder.getFrontierChunks().size() + ": " + n);
//                }
//            }
//        }
    }

    /**
     * Called whenever the given frontier chunk was destroyed and the chunks frontier must be updated.
     *
     * @param frontierChunk
     */
//    public static int onFrontierChunkRemoved(Chunk frontierChunk) {
//        int cx = frontierChunk.cx, cz = frontierChunk.cz;
//        double d = ChunkDistance.distToChunk(cx, cz);
//        return onFrontierChunkRemoved(cx - 1, cz, d) + onFrontierChunkRemoved(cx + 1, cz, d) + onFrontierChunkRemoved(cx, cz - 1, d)
//                + onFrontierChunkRemoved(cx, cz + 1, d);
//    }

    private static int onFrontierChunkRemoved(int cx, int cz, double d) {
//        Chunk n = chunkHolder.getChunkByCoordinate(new Vector2i(cx, cz));
//        if (n != null) {
//            n.neighbors--;
//            if (!chunkHolder.getFrontierChunks().contains(n) && (ChunkDistance.chunkInRenderDistance(cx, cz) || ChunkDistance.distToChunk(cx, cz) < d)) {
//                chunkHolder.getFrontierChunks().add(n);
//                if (GameProperties.DEBUG) {
//                    System.out.println("Added retreating frontier chunk #" + chunkHolder.getFrontierChunks().size() + ": " + n);
//                }
//                return 1;
//            }
//        }
        return 0;
    }

    public static void remove(Chunk chunk) {
//        chunkManager.getFrontierChunks().remove(chunk);
    }
}
