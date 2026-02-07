package ru.mikst74.mikstcraft;

import ru.mikst74.mikstcraft.model.chunk.VoxelField;
import ru.mikst74.mikstcraft.model.coo.VoxelCoo;
import ru.mikst74.mikstcraft.util.generation.FloodFill3D;
import ru.mikst74.mikstcraft.util.floodfill.ChunkVisibility;
import ru.mikst74.mikstcraft.util.floodfill.VoxelConnectivity;

import static ru.mikst74.mikstcraft.dictionary.BlockTypeDictionary.DEFAULT_BLOCK;
import static ru.mikst74.mikstcraft.util.time.Profiler.*;

public class TimeTester {
    public static void main(String[] args) throws Exception {
//        voxelRecalcAllBitMaskTest();
        floodFillTest();
    }

    private static void floodFillTest() {
        int[][][] c = new int[32][32][32];
        for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 32; y++) {
                for (int z = 0; z < 32; z++) {

//                    c[x][y][z] = z == 1 && x == 1 ? 0 : 7;
//                    c[x][y][z] = 7; // all solid block
                     c[x][y][z] = 0; // all air block
//                    c[x][y][z] = (x == 10 || y == 10 || z == 10) ? 7 : 0;

                }
            }
        }
        FloodFill3D ff = new FloodFill3D();
        VoxelConnectivity vc = new VoxelConnectivity();
        ChunkVisibility cv = new ChunkVisibility();
        start();
        profile("test", () -> {
            for (int i = 0; i < 10000; i++) {
//                ff.floodFill(c, 0, 0, 0,0);
                ChunkVisibility.VisibilityResult res = cv.calculateFull(new int[16][16][16]);
//                System.out.println(res);
            }
        });

        printProfile();
    }

    private static void voxelRecalcAllBitMaskTest() {
        VoxelField vf = new VoxelField();
        VoxelCoo c = new VoxelCoo();

        vf.enableLoadingMode();
        while (c.iterateX()) {
            while (c.iterateY()) {
                while (c.iterateZ()) {
                    if (
                        //Chess
//                            (c.getX() & 1) == 0 &&
//                                    (c.getY() & 1) == 0 &&
//                                    (c.getZ() & 1) == 0

                            (c.getX() >= c.getY() && c.getZ() >= c.getY())
                    ) {
                        vf.store(c, DEFAULT_BLOCK);
                    }
                }
            }
        }
        start();
        profile("test", () -> {
            for (int i = 0; i < 10000; i++) {
                vf.recalcAllBitMask();
            }
        });

        printProfile();
    }
}
