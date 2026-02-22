package ru.mikst74.mikstcraft.world.generator;//package org.lwjgl.demo.mikstcraft.world.generator;

import ru.mikst74.mikstcraft.dictionary.BlockTypeDictionary;
import ru.mikst74.mikstcraft.dictionary.BlockTypeInfo;
import ru.mikst74.mikstcraft.model.chunk.VoxelField;
import ru.mikst74.mikstcraft.model.coo.ChunkCoo;
import ru.mikst74.mikstcraft.model.coo.VoxelCoo;

public class NearEgdesTerrain3DGenerator implements WorldMapGenerator {
    @Override
    public VoxelField createVoxelField(ChunkCoo chunkCoo) {
        VoxelField res = new VoxelField();
        res.enableLoadingMode();
        VoxelCoo c = new VoxelCoo(0, 0, 0);
        BlockTypeInfo block = BlockTypeDictionary.getBlockTypeInfo(3);
        while (c.iterateX()) {
//            System.out.println("generate X="+c.getX());
            while (c.iterateY()) {
//            System.out.println("generate Y="+c.getY());
                while (c.iterateZ()) {
//            System.out.println("generate Z="+c);
                    if (
                            (c.getX() == 1 && ((c.getZ() > 1) && (c.getZ() < 31)) && ((c.getY() > 1) && (c.getY() < 31))) ||
                                    (c.getX() == 30 && ((c.getZ() > 1) && (c.getZ() < 31)) && ((c.getY() > 1) && (c.getY() < 31))) ||

                                    (c.getY() == 1 && ((c.getZ() > 1) && (c.getZ() < 31)) && ((c.getX() > 1) && (c.getX() < 31))) ||
                                    (c.getY() == 30 && ((c.getZ() > 1) && (c.getZ() < 31)) && ((c.getX() > 1) && (c.getX() < 31)))
                    ) {
                        res.store(c, block);
                    }
                    ;
                }
            }
        }

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
