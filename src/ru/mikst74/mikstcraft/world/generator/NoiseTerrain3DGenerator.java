package ru.mikst74.mikstcraft.world.generator;//package org.lwjgl.demo.mikstcraft.world.generator;

import ru.mikst74.mikstcraft.dictionary.BlockTypeDictionary;
import ru.mikst74.mikstcraft.model.chunk.VoxelField;
import ru.mikst74.mikstcraft.model.coo.ChunkCoo;
import ru.mikst74.mikstcraft.model.coo.VoxelCoo;
import ru.mikst74.mikstcraft.util.generation.PerlinNoise3D;

public class NoiseTerrain3DGenerator implements WorldMapGenerator {
    @Override
    public VoxelField createVoxelField(ChunkCoo chunkCoo) {
        VoxelField res = new VoxelField();
        res.enableLoadingMode();
        VoxelCoo c = new VoxelCoo(0, 0, 0);
        double scale = 0.09;
        while (c.iterateX()) {
//            System.out.println("generate X=" + c.getX());
            while (c.iterateY()) {
//                System.out.println("generate Y=" + c.getY());
                while (c.iterateZ()) {
                    double val = PerlinNoise3D.noise(
                            (chunkCoo.getX() * 32 + c.getX()) * scale,
                            (chunkCoo.getY() * 32 + c.getY()) * scale,
                            (chunkCoo.getZ() * 32 + c.getZ()) * scale);
                    float norm = (float) ((val + 1) * 50);
//                    norm = Math.max(1, Math.min(10, norm)); // Clamp values

                    //System.out.println("generate Z=" + c + " val=" + val + " normval=" + norm);
                    if (norm > 45) {
                        res.store(c, BlockTypeDictionary.getBlockTypeInfo((int) Math.max(1, Math.min(4, (Math.exp(norm / 5) + 1)))));
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
