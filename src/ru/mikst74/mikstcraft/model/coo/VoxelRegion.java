package ru.mikst74.mikstcraft.model.coo;

import lombok.Getter;

@Getter
public class VoxelRegion {
    private final VoxelCoo start;
    private final VoxelCoo size;

    public VoxelRegion() {
        this.start = new VoxelCoo();
        this.size  = new VoxelCoo();
    }

    public VoxelRegion(VoxelCoo start, VoxelCoo size) {
        this.start = start;
        this.size  = size;
    }
}
