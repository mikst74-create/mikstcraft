package ru.mikst74.mikstcraft.world.generator;//package org.lwjgl.demo.mikstcraft.world.generator;

import ru.mikst74.mikstcraft.dictionary.BlockTypeDictionary;
import ru.mikst74.mikstcraft.dictionary.BlockTypeInfo;
import ru.mikst74.mikstcraft.model.chunk.VoxelField;
import ru.mikst74.mikstcraft.model.coo.ChunkCoo;
import ru.mikst74.mikstcraft.model.coo.VoxelCoo;

public class EightVoxelPerChunkCube2x2Terrain3DGenerator implements WorldMapGenerator {
    @Override
    public VoxelField createVoxelField(ChunkCoo chunkCoo) {
        VoxelField res = new VoxelField();
        res.enableLoadingMode();
        BlockTypeInfo bti1 = BlockTypeDictionary.getBlockTypeInfo(1);
        BlockTypeInfo bti3 = BlockTypeDictionary.getBlockTypeInfo(3);
        res.store(new VoxelCoo(-1, -1, -1), bti3);

        // 0,0,0
//        res.store(new VoxelCoo(0, 0, 0), bti3);
//        res.store(new VoxelCoo(0, 0, 1), bti1);
//        res.store(new VoxelCoo(1, 0, 0), bti1);
//        res.store(new VoxelCoo(1, 0, 1), bti1);
//        res.store(new VoxelCoo(0, 1, 0), bti1);
//        res.store(new VoxelCoo(0, 1, 1), bti1);
//        res.store(new VoxelCoo(1, 1, 0), bti1);
//        res.store(new VoxelCoo(1, 1, 1), bti1);
    // 0,0,0
        res.store(new VoxelCoo(1, 1, 1), bti3);
        res.store(new VoxelCoo(1, 1, 2), bti1);
        res.store(new VoxelCoo(2, 1, 1), bti1);
        res.store(new VoxelCoo(2, 1, 2), bti1);
        res.store(new VoxelCoo(1, 2, 1), bti1);
        res.store(new VoxelCoo(1, 2, 2), bti1);
        res.store(new VoxelCoo(2, 2, 1), bti1);
        res.store(new VoxelCoo(2, 2, 2), bti1);

        return res;
    }

}
