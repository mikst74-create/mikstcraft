package ru.mikst74.mikstcraft.render.chunk;

import ru.mikst74.mikstcraft.model.NeighborCode;
import ru.mikst74.mikstcraft.model.coo.ChunkCoo;

import java.util.*;
import java.util.function.Function;

import static ru.mikst74.mikstcraft.model.NeighborCode.*;

public class VisibilityManager {
    private Function<ChunkCoo, RenderedChunk> loadChunk;
//    // Standard 6 directions in a voxel world
//    public enum Face {NORTH, SOUTH, EAST, WEST, UP, DOWN}

//    class Chunk {
//        int x, y, z;
//        // Precomputed connectivity: can light/vision pass from face A to face B?
//        boolean[][] faceConnectivity = new boolean[6][6];
//        boolean     isVisible        = false;
//
//        public List<NeighborInfo> getNeighbors() { /* Returns adjacent chunks and the entry face */ }
//    }


    public VisibilityManager(Function<ChunkCoo, RenderedChunk> loadChunk) {
        this.loadChunk = loadChunk;
    }

    public void computeVisibleChunks(ChunkCoo startChunkCoo) {
        RenderedChunk startChunk = loadChunk.apply(startChunkCoo);
        if (startChunk != null) {
            startChunk.isVisible = true;
            // X
            if (startChunk.getFacesVisibility().hasExit(XM)) {
                computeVisibleChunksEighthPart(startChunk, XP, new ArrayList<>(Arrays.asList(XM, ZP, YP)));
                computeVisibleChunksEighthPart(startChunk, XP, new ArrayList<>(Arrays.asList(XM, ZP, YM)));
                computeVisibleChunksEighthPart(startChunk, XP, new ArrayList<>(Arrays.asList(XM, ZM, YP)));
                computeVisibleChunksEighthPart(startChunk, XP, new ArrayList<>(Arrays.asList(XM, ZM, YM)));
            }
            if (startChunk.getFacesVisibility().hasExit(XM)) {
                computeVisibleChunksEighthPart(startChunk, XM, new ArrayList<>(Arrays.asList(XM, ZP, YP)));
                computeVisibleChunksEighthPart(startChunk, XM, new ArrayList<>(Arrays.asList(XM, ZP, YM)));
                computeVisibleChunksEighthPart(startChunk, XM, new ArrayList<>(Arrays.asList(XM, ZM, YP)));
                computeVisibleChunksEighthPart(startChunk, XM, new ArrayList<>(Arrays.asList(XM, ZM, YM)));
            }

            // Y
            if (startChunk.getFacesVisibility().hasExit(YP)) {
                computeVisibleChunksEighthPart(startChunk, YP, new ArrayList<>(Arrays.asList(YP, ZP, XP)));
                computeVisibleChunksEighthPart(startChunk, YP, new ArrayList<>(Arrays.asList(YP, ZP, XM)));
                computeVisibleChunksEighthPart(startChunk, YP, new ArrayList<>(Arrays.asList(YP, ZM, XP)));
                computeVisibleChunksEighthPart(startChunk, YP, new ArrayList<>(Arrays.asList(YP, ZM, XM)));
            }
            if (startChunk.getFacesVisibility().hasExit(YM)) {
                computeVisibleChunksEighthPart(startChunk, YM, new ArrayList<>(Arrays.asList(YM, ZP, XP)));
                computeVisibleChunksEighthPart(startChunk, YM, new ArrayList<>(Arrays.asList(YM, ZP, XM)));
                computeVisibleChunksEighthPart(startChunk, YM, new ArrayList<>(Arrays.asList(YM, ZM, XP)));
                computeVisibleChunksEighthPart(startChunk, YM, new ArrayList<>(Arrays.asList(YM, ZM, XM)));
            }

            // Z
            if (startChunk.getFacesVisibility().hasExit(ZP)) {
                computeVisibleChunksEighthPart(startChunk, ZP, new ArrayList<>(Arrays.asList(ZP, YP, XP)));
                computeVisibleChunksEighthPart(startChunk, ZP, new ArrayList<>(Arrays.asList(ZP, YP, XM)));
                computeVisibleChunksEighthPart(startChunk, ZP, new ArrayList<>(Arrays.asList(ZP, YM, XP)));
                computeVisibleChunksEighthPart(startChunk, ZP, new ArrayList<>(Arrays.asList(ZP, YM, XM)));
            }
            if (startChunk.getFacesVisibility().hasExit(ZM)) {
                computeVisibleChunksEighthPart(startChunk, ZM, new ArrayList<>(Arrays.asList(ZM, YP, XP)));
                computeVisibleChunksEighthPart(startChunk, ZM, new ArrayList<>(Arrays.asList(ZM, YP, XM)));
                computeVisibleChunksEighthPart(startChunk, ZM, new ArrayList<>(Arrays.asList(ZM, YM, XP)));
                computeVisibleChunksEighthPart(startChunk, ZM, new ArrayList<>(Arrays.asList(ZM, YM, XM)));
            }
        }
    }

    public void computeVisibleChunksEighthPart(RenderedChunk startChunk, NeighborCode startNC, List<NeighborCode> possibleNextNCList) {
        if (startChunk == null) {
            return;
        }
        Queue<ChunkTraversal> queue = new LinkedList<>();
        Set<RenderedChunk> visited = new HashSet<>();

        // Start BFS from player's chunk
        visited.add(startChunk);

        // Initial entry: check all faces of the starting chunk
        queue.add(new ChunkTraversal(startChunk, startNC, possibleNextNCList));
        ChunkCoo currentCoo = new ChunkCoo().assign(startChunk.getChunk().getCoo());
        ChunkCoo neighborCoo = new ChunkCoo();
        while (!queue.isEmpty()) {
            ChunkTraversal current = queue.poll();
            RenderedChunk chunk = current.chunk;
            NeighborCode entryFace = current.entryFace;

            // Check every possible EXIT face from this chunk
            for (NeighborCode exitFace : current.possibleExitFace) {
                // CORE LOGIC: Only traverse to neighbor if a path exists through this chunk
                if (chunk.getChunk().getVoxelField().getAirFlowThroughChunk().check(entryFace, exitFace)) {
                    RenderedChunk neighbor = loadChunk.apply(neighborCoo.assign(chunk.getChunk().getCoo()).step(exitFace));

                    if (neighbor != null && !visited.contains(neighbor) && neighbor.inFrustrum) {
                        neighbor.isVisible = true;
                        visited.add(neighbor);

                        // Enter neighbor through the opposite of the exit face
                        NeighborCode neighborEntry = exitFace.getOpposite();
                        queue.add(new ChunkTraversal(neighbor, neighborEntry, current.possibleExitFace));
                    }
//                    }
                }
            }
        }
    }
}
