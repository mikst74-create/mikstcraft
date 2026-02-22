package ru.mikst74.mikstcraft.world.generator;

import ru.mikst74.mikstcraft.model.coo.ChunkCoo;
import ru.mikst74.mikstcraft.model.chunk.VoxelField;

public interface WorldMapGenerator {
    /**
     * Create a voxel field for a chunk at the given chunk position.
     *
     * @param coo the coordinate of the chunk position (in whole chunks)
     */
    VoxelField createVoxelField(ChunkCoo coo);

}
