package ru.mikst74.mikstcraft.model.chunk;


import lombok.Getter;
import ru.mikst74.mikstcraft.dictionary.BlockTypeInfo;
import ru.mikst74.mikstcraft.model.NeighborCode;
import ru.mikst74.mikstcraft.model.coo.ChunkCoo;
import ru.mikst74.mikstcraft.model.coo.VoxelCoo;
import ru.mikst74.mikstcraft.model.coo.WorldCoo;

import java.io.Serializable;

import static ru.mikst74.mikstcraft.model.coo.WCVConverter.staticWCV;

/**
 * Represents a chunk.
 * Chuck is a cube 16x16x16
 */
public class Chunk implements Serializable {
    @Getter
    public final           ChunkCoo coo;
    @Getter
    public transient final WorldCoo wCoo;

    @Getter
    private final VoxelField voxelField;

    @Getter
    private long lastUpdateTime;
    private long lastSavedTime;


    public Chunk(ChunkCoo coo) {
        System.out.println("New chunk created " + coo);
        this.coo = new ChunkCoo(coo);
        int shift = staticWCV.getV().getSideShift();
        wCoo       = new WorldCoo(coo.getX() << shift, coo.getY() << shift, coo.getZ() << shift);
        voxelField = new VoxelField();
        voxelField.setUpdateCallback(this::actualizeUpdateTime);
    }
//
//    public void setNeighborChunk(NeighborCode neighborCode, Chunk neighborChunk) {
//        neighborChunks[neighborCode.getI()]                         = neighborChunk;
//        voxelField.getNeighborVoxelFieldList()[neighborCode.getI()] = null;
//        if (neighborChunk != null) {
//            voxelField.getNeighborVoxelFieldList()[neighborCode.getI()] = neighborChunk.getVoxelField();
//            neighborChunk.setNeighborChunkOnce(neighborCode.getOpposite(), this);
//            neighborChunk.getVoxelField().getNeighborVoxelFieldList()[neighborCode.getOpposite().getI()] = this.getVoxelField();
//            neighborChunk.actualizeUpdateTime();
//        }
//        actualizeUpdateTime();
//
//    }
//
//    public void setNeighborChunkOnce(NeighborCode neighborCode, Chunk neighborChunk) {
//        neighborChunks[neighborCode.getI()]                         = neighborChunk;
//        voxelField.getNeighborVoxelFieldList()[neighborCode.getI()] = null;
//        if (neighborChunk != null) {
//            voxelField.getNeighborVoxelFieldList()[neighborCode.getI()] = neighborChunk.getVoxelField();
//        }
//        actualizeUpdateTime();
//
//    }


    @Override
    public String toString() {
        return "Chunk[" + coo.getX() + "," + coo.getY() + "," + coo.getZ() + "]";
    }

    public BlockTypeInfo getVoxel(VoxelCoo coo) {
        return voxelField.loadBTI(coo);
    }

    public int getFaceInfo(VoxelCoo coo, NeighborCode nc) {
        return voxelField.getFaceInfo(coo, nc);
    }

    public void setVoxel(VoxelCoo coo, BlockTypeInfo v) {
        voxelField.store(coo, v);
        actualizeUpdateTime();
//        for (NeighborCode nc : NeighborCode.values()) {
//            if (coo.onEdge(nc)) {
//                Chunk neighborChunk = neighborChunks[nc.getI()];
//                if (neighborChunk != null) {
//                    neighborChunk.actualizeUpdateTime();
//                }
//            }
//        }
    }

    public void setVoxelFieldData(VoxelField newVoxelField) {
        this.voxelField.uploadFieldFrom(newVoxelField);
    }

    public void actualizeUpdateTime() {
        lastUpdateTime = System.nanoTime();
    }

    public void enableLoadingMode() {
        voxelField.enableLoadingMode();
    }

    public void disableLoadingMode() {
        voxelField.disableLoadingMode();
    }

    public boolean isNotSaved() {
        return lastUpdateTime > lastSavedTime;
    }

    public void markAsSaved() {
        lastSavedTime = System.nanoTime();
    }

//    public Chunk getNeighbor(NeighborCode nc) {
//        return neighborChunks[nc.getI()];
//    }
}
