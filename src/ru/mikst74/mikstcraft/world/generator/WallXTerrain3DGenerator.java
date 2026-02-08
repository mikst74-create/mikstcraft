package ru.mikst74.mikstcraft.world.generator;//package org.lwjgl.demo.mikstcraft.world.generator;

import ru.mikst74.mikstcraft.dictionary.BlockTypeDictionary;
import ru.mikst74.mikstcraft.dictionary.BlockTypeInfo;
import ru.mikst74.mikstcraft.model.chunk.VoxelField;
import ru.mikst74.mikstcraft.model.coo.ChunkCoo;
import ru.mikst74.mikstcraft.model.coo.VoxelCoo;

public class WallXTerrain3DGenerator implements WorldMapGenerator {
    @Override
    public VoxelField createVoxelField(ChunkCoo chunkCoo) {
        VoxelField res = new VoxelField();
        BlockTypeInfo block = BlockTypeDictionary.getBlockTypeInfo(5);
        res.enableLoadingMode();
        if (chunkCoo.getX() == 0 && chunkCoo.getY() == 0 && chunkCoo.getZ() == 0) {
            for (int u = 0; u <= 15; u++) {
                for (int v = 0; v <= 15; v++) {
                    res.store(new VoxelCoo(0, u, v), block);

                }
            }
        }
//        res.store(new VoxelCoo(0,0,0), (short) 3);
//        res.store(new VoxelCoo(1,0,1), (short) 1);
//        res.store(new VoxelCoo(1,0,2), (short) 1);
//        res.store(new VoxelCoo(2,0,1), (short) 1);
//        res.store(new VoxelCoo(2,0,2), (short) 1);
//        res.store(new VoxelCoo(1,0,1), (short) 1);
//        res.store(new VoxelCoo(0,1,0), (short) 1);
//        res.store(new VoxelCoo(0,1,1), (short) 1);
//        res.store(new VoxelCoo(1,1,0), (short) 1);
//        res.store(new VoxelCoo(1,1,1), (short) 1);
//        res.store(new VoxelCoo(0,0,1), (short) 1);
//        res.store(new VoxelCoo(1,0,0), (short) 1);
//        res.store(new VoxelCoo(2,0,0), (short) 1);
//        res.store(new VoxelCoo(1,1,3), (short) 1);
//        res.store(new VoxelCoo(1,2,2), (short) 1);
//        res.store(new VoxelCoo(1,2,3), (short) 1);
//        res.store(new VoxelCoo(2,0,1), (short) 1);
//        res.store(new VoxelCoo(2,0,2), (short) 1);
//        res.store(new VoxelCoo(2,0,3), (short) 3);
//        res.store(new VoxelCoo(2,0,4), (short) 3);
//        res.store(new VoxelCoo(2,0,5), (short) 3);
//        res.store(new VoxelCoo(2,0,6), (short) 5);


        return res;
    }

    //    /**
//     * Create a voxel field for a chunk at the given chunk position.
//     *
//     * @param cx the x coordinate of the chunk position (in whole chunks)
//     * @param cz the z coordinate of the chunk position (in whole chunks)
//     */
//    public VoxelField createVoxelField(int cx, int cz) {
//        byte[] field = new byte[(GameProperties.CHUNK_SIZE + 2) * (GameProperties.CHUNK_HEIGHT + 2) * (GameProperties.CHUNK_SIZE + 2)];
//        int num = 0;
//        int y = 2;
//        for (int z = 0; z < GameProperties.CHUNK_SIZE; z++) {
//            for (int x = 0; x < GameProperties.CHUNK_SIZE; x++) {
//                for (int y0 = 0; y0 <= y; y0++) {
//                    field[VoxelField.idx(x, y0, z)] = (byte) (1);
//                    num++;
//                }
//            }
//        }
//
//        VoxelField res = new VoxelField();
//        res.ny = y;
//        res.py = y;
//        res.num = num;
//        res.field = field;
//        return res;
//    }

}
