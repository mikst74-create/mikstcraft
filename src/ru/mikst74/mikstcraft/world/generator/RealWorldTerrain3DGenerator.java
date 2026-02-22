package ru.mikst74.mikstcraft.world.generator;//package org.lwjgl.demo.mikstcraft.world.generator;

import ru.mikst74.mikstcraft.dictionary.BlockTypeDictionary;
import ru.mikst74.mikstcraft.model.chunk.VoxelField;
import ru.mikst74.mikstcraft.model.coo.ChunkCoo;
import ru.mikst74.mikstcraft.model.coo.VoxelCoo;
import ru.mikst74.mikstcraft.util.generation.PerlinNoise3D;

import static ru.mikst74.mikstcraft.dictionary.BlockTypeDictionary.AIR_BLOCK;
import static ru.mikst74.mikstcraft.dictionary.BlockTypeDictionary.DEFAULT_BLOCK;

public class RealWorldTerrain3DGenerator implements WorldMapGenerator {
    @Override
    public VoxelField createVoxelField(ChunkCoo chunkCoo) {
        VoxelField res = new VoxelField();
        res.enableLoadingMode();
        VoxelCoo c = new VoxelCoo(0, 0, 0);
        double scale = 0.009;
        if (chunkCoo.getY() >= -1 & chunkCoo.getY() <= 2) {
            while (c.iterateX()) {
                while (c.iterateY()) {
                    while (c.iterateZ()) {
                        if (chunkCoo.getY() >= 0 & chunkCoo.getY() <= 2) {
                            double val = PerlinNoise3D.noise(
                                    (chunkCoo.getX() * 32 + c.getX()) * scale,
                                    (chunkCoo.getY() * 32 + c.getY()) * scale,
                                    (chunkCoo.getZ() * 32 + c.getZ()) * scale);
                            float norm = (float) ((val + 1) * 50);
                            if (norm < 45) {
                                res.store(c, BlockTypeDictionary.getBlockTypeInfo((int) Math.max(1, Math.min(4, (Math.exp(norm / 5) + 1)))));
                            }
                        }
                        if (chunkCoo.getY() < 0) {
                            res.store(c, DEFAULT_BLOCK);
                        }
                        if (chunkCoo.getY() > 2) {
                            res.store(c, AIR_BLOCK);
                        }
                    }
                }
            }
        }
        res.disableLoadingMode();
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
