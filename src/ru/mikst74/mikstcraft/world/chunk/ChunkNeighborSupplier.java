package ru.mikst74.mikstcraft.world.chunk;

import lombok.Getter;
import ru.mikst74.mikstcraft.model.chunk.Chunk;
import ru.mikst74.mikstcraft.model.coo.ChunkCoo;
import ru.mikst74.mikstcraft.model.coo.VoxelCoo;
import ru.mikst74.mikstcraft.model.coo.VoxelRegion;

import static ru.mikst74.mikstcraft.model.coo.VoxelCoo.CHUNK_SIZE;

/**
 * Created by Mikhail Krinitsyn on 10.01.2026
 */
@Getter
public class ChunkNeighborSupplier {
    private final ChunkManager chunkManager;

    public ChunkNeighborSupplier(ChunkManager chunkManager) {
        this.chunkManager = chunkManager;
    }

    public static void ensureNeighbor(Chunk chunk) {
//        for (NeighborCode nc : NeighborCode.values()) {
//            chunk.setNeighborChunk(nc, chunkManager.getChunk(chunk.getCoo().getNB(nc)));
//        }

    }


    public void copyVoxelFieldFromNeighborAfterChunkCreated(Chunk chunk) {
        int side = CHUNK_SIZE - 1;
        ChunkCoo neighborCoo = new ChunkCoo();
        VoxelCoo dstStartCoo = new VoxelCoo();
        VoxelRegion srcRegion = new VoxelRegion();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    // Skip current chunk
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }

                    Chunk neighbor = chunkManager.getChunkIfExists(neighborCoo.assign(chunk.getCoo()).addX(x).addY(y).addZ(z));
                    if (neighbor != null) {
                        int sizeX = x == 0 ? side : 0;
                        int sizeY = y == 0 ? side : 0;
                        int sizeZ = z == 0 ? side : 0;

                        // Determine intersection area of voxelField as 3drectangle
                        // assign start point in voxel coordinates for source and destination chunk
                        srcRegion.getStart().assign(x < 0 ? side : 0, y < 0 ? side : 0, z < 0 ? side : 0);
                        srcRegion.getSize().assign(sizeX, sizeY, sizeZ);
                        dstStartCoo.assign(
                                x > 0 ? side + 1 : x == 0 ? 0 : -1,
                                y > 0 ? side + 1 : y == 0 ? 0 : -1,
                                z > 0 ? side + 1 : z == 0 ? 0 : -1);
                        // Copy from src(neighbor  chunk) to dst (current chunk)
                        copyVoxelFieldRegion(neighbor, srcRegion, chunk, dstStartCoo);
                        // Copy reverse, from current chunk to neighbor
                        srcRegion.getStart().assign((-1 * x) < 0 ? side : 0, (-1 * y) < 0 ? side : 0, (-1 * z) < 0 ? side : 0);
                        dstStartCoo.assign(
                                (-1 * x) > 0 ? side + 1 : (-1 * x) == 0 ? 0 : -1,
                                (-1 * y) > 0 ? side + 1 : (-1 * y) == 0 ? 0 : -1,
                                (-1 * z) > 0 ? side + 1 : (-1 * z) == 0 ? 0 : -1);
                        copyVoxelFieldRegion(chunk, srcRegion, neighbor, dstStartCoo);
                    }
                }
            }
        }
    }

    private static void copyVoxelFieldRegion(Chunk srcChunk, VoxelRegion srcRegion, Chunk dstChunk, VoxelCoo dstStartCoo) {
        VoxelCoo dstCurrentCoo = new VoxelCoo();
        VoxelCoo srcCurrentCoo = new VoxelCoo();
//        System.out.println("**************************");
//        System.out.println("* Copy from " + srcChunk + " start:" + srcRegion.getStart() + " size:" + srcRegion.getSize());
//        System.out.println("* Copy to " + dstChunk + " start:" + dstStartCoo);
        dstChunk.enableLoadingMode();
        for (int vx = 0; vx <= srcRegion.getSize().getX(); vx++) {
            for (int vy = 0; vy <= srcRegion.getSize().getY(); vy++) {
                for (int vz = 0; vz <= srcRegion.getSize().getZ(); vz++) {
                    dstChunk.setVoxel(dstCurrentCoo.assign(dstStartCoo).addXYZ(vx, vy, vz),
                            srcChunk.getVoxel(srcCurrentCoo.assign(srcRegion.getStart()).addXYZ(vx, vy, vz)));
                }
            }
        }
        dstChunk.disableLoadingMode();
    }
}
