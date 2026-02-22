//package org.lwjgl.demo.mikstcraft.meshing;
//
//import lombok.SneakyThrows;
//import org.lwjgl.demo.mikstcraft.model.NeighborCode;
//import org.lwjgl.demo.mikstcraft.model.chunk.Chunk;
//import org.lwjgl.demo.mikstcraft.model.coo.VoxelCoo;
//
//import static java.lang.Math.max;
//import static java.lang.Math.min;
//
///**
// * Implementation of Greedy Meshing that takes into account the minimum/maximum Y coordinate of
// * active voxels to speedup the meshing.
// * <p>
// * It also generates "neighbor configurations" for simple AO inside of the face value.
// */
//public class GreedyMeshing2 {
//
//    /**
//     * Pre-computed lookup table for neighbor configurations depending on whether any particular of the
//     * three neighbors of the possible four vertices of a face is occupied or not.
//     */
//    private static final int[] NEIGHBOR_CONFIGS = computeNeighborConfigs();
//    /**
//     * We limit the length of merged faces to 32, to be able to store 31 as 5 bits.
//     */
//    private static final int MAX_MERGE_LENGTH = 32;
//    private static final int MAX_DET = 32;
//
//    private final int[] m;
//    private final int dx, dy, dz;
//    //    private byte[] vs;
//    private Chunk chunk;
//    private int count;
//
//
//    /*
//
//         X: -1    0     +1
//       Y:+-------+-------+-------+
//     +1  | nxpy:4| cxpy:5| pxpy:6|
//         +-------+-------+-------+
//      0  | nxcy:3|  X,Y  | pxcy:7|
//         +-------+-------+-------+
//     -1  | nxny:2| cxny:1| pxny:8|
//         +-------+-------+-------+
//                                                  19  18  17  16  15  14  13  12  11  10   9   8   7   6   5   4   3   2   1   0
//         +---+---+---+---+---+---o---+---+---+---+---+---+---+---o---+---+---+---+---+---+---+---o---+---+---+---+---+---+---+---+
//         |   |   |   |   |   |   |   |   |   |   |pc .pp .cp |nc .np .cp |pc .pn .cn |nc .nn .cn |   |   |   |   |   |   |   |   |
//         +---+---+---+---+---+---o---+---+---+---+---+---+---+---o---+---+---+---+---+---+---+---o---+---+---+---+---+---+---+---+
//     */
//    private static int[] computeNeighborConfigs() {
//        int[] offs = new int[256];
//        for (int i = 0; i < 256; i++) {
//            boolean cxny = (i & 1) == 1,
//                    nxny = (i & 1 << 1) == 1 << 1,
//                    nxcy = (i & 1 << 2) == 1 << 2,
//                    nxpy = (i & 1 << 3) == 1 << 3,
//                    cxpy = (i & 1 << 4) == 1 << 4,
//                    pxpy = (i & 1 << 5) == 1 << 5,
//                    pxcy = (i & 1 << 6) == 1 << 6,
//                    pxny = (i & 1 << 7) == 1 << 7;
//            offs[i] = (cxny ? 1 : 0) + (nxny ? 2 : 0) + (nxcy ? 4 : 0)
//                    | (cxny ? 1 : 0) + (pxny ? 2 : 0) + (pxcy ? 4 : 0) << 3
//                    | (cxpy ? 1 : 0) + (nxpy ? 2 : 0) + (nxcy ? 4 : 0) << 6
//                    | (cxpy ? 1 : 0) + (pxpy ? 2 : 0) + (pxcy ? 4 : 0) << 9;
//            offs[i] = offs[i] << 8;
//        }
//        return offs;
//    }
//
//    public GreedyMeshing2(int dx, int dy, int dz) {
//        this.dx = dx;
//        this.dy = dy;
//        this.dz = dz;
//        this.m = new int[max(dx, dy) * max(dx, dy) * max(dy, dz)];
//    }
//
//    private short at(int x, int y, int z) {
//        VoxelCoo c = new VoxelCoo(x, y, z);
////        AtomicInteger atom = new AtomicInteger();
////        Profiler.profile("GreedyMeshing.at()", () -> {
//        short res;
//        NeighborCode nc = c.getNeighborEdge();
//        if (nc != null) {
//            c.changeToNB(nc);
//            res = chunk.getNeighborChunk(nc).getVoxel(c);
//        } else
//            res = chunk.getVoxel(c);
//
////        if (x == -1) {
////            Chunk neighborChunk = chunk.getNeighborChunk(XM);
////            c.changeToNB(XM);
////            res = neighborChunk.getVoxel(c);
////        } else if (x == GameProperties.CHUNK_SIZE) {
////            Chunk xp1 = chunk.getNeighborXP1();
////            res = xp1.getVoxel(0, y, z);
////        } else if (z == -1) {
////            Chunk zm1 = chunk.getNeighborZM1();
////            res = zm1.getVoxel(x, y, GameProperties.CHUNK_SIZE_M1);
////        } else if (z == GameProperties.CHUNK_SIZE) {
////            Chunk zp1 = chunk.getNeighborZP1();
////            res = zp1.getVoxel(x, y, 0);
////        } else
////            res = chunk.getVoxel(x, y, z);
////            atom.set(res);
////        });
////        byte res = (byte) atom.get();
//        return res == 7 ? 0 : res;
//    }
//
//    private short atWithTransparent(int x, int y, int z) {
//        byte res = 0;
////        if (x == -1) {
////            Chunk xm1 = chunk.getNeighborXM1();
////            res = xm1.getVoxel(GameProperties.CHUNK_SIZE_M1, y, z);
////        } else if (x == GameProperties.CHUNK_SIZE) {
////            Chunk xp1 = chunk.getNeighborXP1();
////            res = xp1.getVoxel(0, y, z);
////        } else if (z == -1) {
////            Chunk zm1 = chunk.getNeighborZM1();
////            res = zm1.getVoxel(x, y, GameProperties.CHUNK_SIZE_M1);
////        } else if (z == GameProperties.CHUNK_SIZE) {
////            Chunk zp1 = chunk.getNeighborZP1();
////            res = zp1.getVoxel(x, y, 0);
////        } else
////            res = chunk.getVoxel(x, y, z);
//        return res;
//    }
//
//
//    //    public int mesh(byte[] vs, FaceConsumer faces) {
//    @SneakyThrows
//    public int mesh(Chunk chunk, FaceConsumer faces) {
////        this.vs = vs;
//        this.chunk = chunk;
//
//        meshX(faces);
//        meshY(faces);
//        meshZ(faces);
//        return count;
//    }
//
//    private void meshX(FaceConsumer faces) {
//        for (int x = 0; x < dx; ) {
//            generateMaskX(x);
//            mergeAndGenerateFacesX(faces, ++x);
//        }
//    }
//
//    private void meshY(FaceConsumer faces) {
//        for (int y = 0; y < dy; ) {
//            generateMaskY(y);
//            mergeAndGenerateFacesY(faces, ++y);
//        }
//    }
//
//    private void meshZ(FaceConsumer faces) {
//        for (int z = 0; z < dz; ) {
//            generateMaskZ(z);
//            mergeAndGenerateFacesZ(faces, ++z);
//        }
//    }
//
//    private void generateMaskX(int x) {
//        int n = 0;
//        for (int z = 0; z < dz; z++)
//            for (int y = 0; y < dy; y++, n++)
//                generateMaskX(x, y, z, n);
//    }
//
//    private void generateMaskY(int y) {
//        int n = 0;
//        for (int x = 0; x < dx; x++)
//            for (int z = 0; z < dz; z++, n++)
//                generateMaskY(x, y, z, n);
//    }
//
//    private void generateMaskZ(int z) {
//        int n = 0;
//        for (int y = 0; y < dy; y++)
//            for (int x = 0; x < dx; x++, n++)
//                generateMaskZ(x, y, z, n);
//    }
//
//    private void generateMaskX(int x, int y, int z, int n) {
//        int a = at(x, y, z), b = at(x + 1, y, z);
//        if (((a == 0) == (b == 0))) {
////        if (((a == 0))) {
//            m[n] = 0;
//        } else if (a != 0) {
//            m[n] = (a & 0xFF) | neighborsX(x + 1, y, z);
//        } else {
//            m[n] = (b & 0xFF) | neighborsX(x, y, z) | 1 << 31;
//        }
//    }
//
//    private int neighborsX(int x, int y, int z) {
//        /* UV = YZ */
//        int n1 = 0;//at(x, y - 1, z - 1) != 0 ? 2 : 0;
//        int n2 = at(x, y - 1, z) != 0 ? 4 : 0;
//        int n3 = 0;//at(x, y - 1, z + 1) != 0 ? 8 : 0;
//        int n0 = at(x, y, z - 1) != 0 ? 1 : 0;
//        int n4 = at(x, y, z + 1) != 0 ? 16 : 0;
//        int n7 = 0;//at(x, y + 1, z - 1) != 0 ? 128 : 0;
//        int n6 = at(x, y + 1, z) != 0 ? 64 : 0;
//        int n5 = 0;//at(x, y + 1, z + 1) != 0 ? 32 : 0;
////        int n1 = z==0?2:at(x, y - 1, z - 1) != 0 ? 2 : 0;
////        int n2 = at(x, y - 1, z) != 0 ? 4 : 0;
////        int n3 = z==31?8:at(x, y - 1, z + 1) != 0 ? 8 : 0;
////        int n0 = z==0?1:at(x, y, z - 1) != 0 ? 1 : 0;
////        int n4 = z==31?16:at(x, y, z + 1) != 0 ? 16 : 0;
////        int n7 = z==0?128:at(x, y + 1, z - 1) != 0 ? 128 : 0;
////        int n6 = at(x, y + 1, z) != 0 ? 64 : 0;
////        int n5 = z==31?32:at(x, y + 1, z + 1) != 0 ? 32 : 0;
////        return NEIGHBOR_CONFIGS[n0 | n1 | n2 | n3 | n4 | n5 | n6 | n7];
//        return NEIGHBOR_CONFIGS[0];
//    }
//
//    private void generateMaskY(int x, int y, int z, int n) {
//        int a = at(x, y, z), b = at(x, y + 1, z);
//        if (((a == 0) == (b == 0))) {
//            m[n] = 0;
//        } else if (a != 0) {
//            m[n] = (a & 0xFF) | neighborsY(x, y + 1, z);
//        } else {
//            m[n] = (b & 0xFF) | (y >= 0 ? neighborsY(x, y, z) : 0) | 1 << 31;
//        }
//    }
//
//    private int neighborsY(int x, int y, int z) {
//        /* UV = ZX */
//        int n1 = 0;//at(x - 1, y, z - 1) != 0 ? 2 : 0;
//        int n2 = at(x, y, z - 1) != 0 ? 4 : 0;
//        int n3 = 0;// at(x + 1, y, z - 1) != 0 ? 8 : 0;
//        int n0 = at(x - 1, y, z) != 0 ? 1 : 0;
//        int n6 = at(x, y, z + 1) != 0 ? 64 : 0;
//        int n4 = at(x + 1, y, z) != 0 ? 16 : 0;
//        int n7 = 0;//at(x - 1, y, z + 1) != 0 ? 128 : 0;
//        int n5 = 0;//at(x + 1, y, z + 1) != 0 ? 32 : 0;
////        int n1 = x==0||z==0?2:at(x - 1, y, z - 1) != 0 ? 2 : 0;
////        int n2 = z==0?4:at(x, y, z - 1) != 0 ? 4 : 0;
////        int n3 = x==0||z==0?2:at(x + 1, y, z - 1) != 0 ? 8 : 0;
////        int n0 = x==0?1:at(x - 1, y, z) != 0 ? 1 : 0;
////        int n6 = z==31?64:at(x, y, z + 1) != 0 ? 64 : 0;
////        int n4 = x==31?16:at(x + 1, y, z) != 0 ? 16 : 0;
////        int n7 = x==0||z==31?128:at(x - 1, y, z + 1) != 0 ? 128 : 0;
////        int n5 = x==31||z==31?32:at(x + 1, y, z + 1) != 0 ? 32 : 0;
//        return NEIGHBOR_CONFIGS[n0 | n1 | n2 | n3 | n4 | n5 | n6 | n7];
////        return NEIGHBOR_CONFIGS[0];
//    }
//
//    private void generateMaskZ(int x, int y, int z, int n) {
//        int a = at(x, y, z), b = at(x, y, z + 1);
//        if (((a == 0) == (b == 0))) {
//            m[n] = 0;
//        } else if (a != 0) {
//            m[n] = (a & 0xFF) | neighborsZ(x, y, z + 1);
//        } else {
//            m[n] = (b & 0xFF) | neighborsZ(x, y, z) | 1 << 31;
//        }
//    }
//
//    private int neighborsZ(int x, int y, int z) {
//        /* UV = XY */
//        int n1 = 0;//at(x - 1, y - 1, z) != 0 ? 2 : 0;
//        int n0 = at(x, y - 1, z) != 0 ? 1 : 0;
//        int n7 = 0;//at(x + 1, y - 1, z) != 0 ? 128 : 0;
//        int n2 = at(x - 1, y, z) != 0 ? 4 : 0;
//        int n6 = at(x + 1, y, z) != 0 ? 64 : 0;
//        int n3 = 0;//at(x - 1, y + 1, z) != 0 ? 8 : 0;
//        int n4 = at(x, y + 1, z) != 0 ? 16 : 0;
//        int n5 = 0;// at(x + 1, y + 1, z) != 0 ? 32 : 0;
////        int n1 = x==0?2:at(x - 1, y - 1, z) != 0 ? 2 : 0;
////        int n0 = at(x, y - 1, z) != 0 ? 1 : 0;
////        int n7 = x==31?128:at(x + 1, y - 1, z) != 0 ? 128 : 0;
////        int n2 = x==0?4:at(x - 1, y, z) != 0 ? 4 : 0;
////        int n6 = x==31?64:at(x + 1, y, z) != 0 ? 64 : 0;
////        int n3 = x==0?8:at(x - 1, y + 1, z) != 0 ? 8 : 0;
////        int n4 = at(x, y + 1, z) != 0 ? 16 : 0;
////        int n5 = x==31?32:at(x + 1, y + 1, z) != 0 ? 32 : 0;
//        return NEIGHBOR_CONFIGS[n0 | n1 | n2 | n3 | n4 | n5 | n6 | n7];
////        return NEIGHBOR_CONFIGS[0];
//    }
//
//    private void mergeAndGenerateFacesX(FaceConsumer faces, int x) {
//        int i, j, n, incr;
//        for (j = 0, n = 0; j < dz; j++)
//            for (i = 0; i < dy; i += incr, n += incr)
//                incr = mergeAndGenerateFaceX(faces, x, n, i, j);
////        for (j = 0; j < dz; j++)
////            for (i = ny; i < py; i++)
////                mergeAndGenerateTransparentFaceX(faces, x, i, j);
//    }
//
//    private void mergeAndGenerateFacesY(FaceConsumer faces, int y) {
//        int i, j, n, incr;
//        for (j = 0, n = 0; j < dx; j++)
//            for (i = 0; i < dz; i += incr, n += incr)
//                incr = mergeAndGenerateFaceY(faces, y, n, i, j);
////        for (j = 0; j < dx; j++)
////            for (i = 0; i < dz; i++)
////                mergeAndGenerateTransparentFaceY(faces, y, i, j);
//    }
//
//    private void mergeAndGenerateFacesZ(FaceConsumer faces, int z) {
//        int i, j, n, incr;
//        for (j = 0, n = 0; j < dy; j++)
//            for (i = 0; i < dx; i += incr, n += incr)
//                incr = mergeAndGenerateFaceZ(faces, z, n, i, j);
////        for (j = ny; j < py; j++)
////            for (i = 0; i < dx; i++)
////                mergeAndGenerateTransparentFaceZ(faces, z, i, j);
//    }
//
//    private int mergeAndGenerateFaceX(FaceConsumer faces, int x, int n, int i, int j) {
//        int mn = m[n];
//        if (mn == 0) {
//            return 1;
//        }
//        int w = min(MAX_DET, determineWidthX(mn, n, i));
//        int h = min(MAX_DET, determineHeightX(mn, n, j, w));
//        faces.consume(i, j, i + w, j + h, x, mn > 0 ? 1 : 0, mn);
//        count++;
//        eraseMask(n, w, h, dy);
//        return w;
//    }
//
//    private void mergeAndGenerateTransparentFaceX(FaceConsumer faces, int x, int i, int j) {
//        int mn = atWithTransparent(x, i, j);
//        if (mn == 7) {
//            faces.consume(i, j, i + 1, j + 1, x, 1, mn);
//            count++;
//        }
//    }
//
//    private int mergeAndGenerateFaceY(FaceConsumer faces, int y, int n, int i, int j) {
//        int mn = m[n];
//        if (mn == 0) {
//            return 1;
//        }
//        int w = min(MAX_DET, determineWidthY(mn, n, i));
//        int h = min(MAX_DET, determineHeightY(mn, n, j, w));
//        faces.consume(i, j, i + w, j + h, y, 2 + (mn > 0 ? 1 : 0), mn);
//        count++;
//        eraseMask(n, w, h, dz);
//        return w;
//    }
//
//    private void mergeAndGenerateTransparentFaceY(FaceConsumer faces, int y, int i, int j) {
//        int mn = atWithTransparent(j, y, i);
//        if (mn == 7) {
//            faces.consume(i, j, i + 1, j + 1, y, 2 + 1, mn);
//            count++;
//        }
//    }
//
//    private int mergeAndGenerateFaceZ(FaceConsumer faces, int z, int n, int i, int j) {
//        int mn = m[n];
//        if (mn == 0) {
//            return 1;
//        }
//        int w = min(MAX_DET, determineWidthZ(mn, n, i));
//        int h = min(MAX_DET, determineHeightZ(mn, n, j, w));
//        faces.consume(i, j, i + w, j + h, z, 4 + (mn > 0 ? 1 : 0), mn);
//        count++;
//        eraseMask(n, w, h, dx);
//        return w;
//    }
//
//    private void mergeAndGenerateTransparentFaceZ(FaceConsumer faces, int z, int i, int j) {
//        int mn = atWithTransparent(i, j, z);
//        if (mn == 7) {
//            faces.consume(i, j, i + 1, j + 1, z, 4 + 1, mn);
//            count++;
//        }
//    }
//
//    private void eraseMask(int n, int w, int h, int d) {
//        for (int l = 0, ls = 0; l < h; l++, ls += d)
//            for (int k = 0; k < w; k++)
//                m[n + k + ls] = 0;
//    }
//
//    private int determineWidthX(int c, int n, int i) {
//        int w = 1;
//        for (; w < MAX_MERGE_LENGTH && i + w < dy && c == m[n + w]; w++)
//            ;
//        return w;
//    }
//
//    private int determineWidthY(int c, int n, int i) {
//        int w = 1;
//        for (; i + w < dz && c == m[n + w]; w++)
//            ;
//        return w;
//    }
//
//    private int determineWidthZ(int c, int n, int i) {
//        int w = 1;
//        for (; i + w < dx && c == m[n + w]; w++)
//            ;
//        return w;
//    }
//
//    private int determineHeightX(int c, int n, int j, int w) {
//        int h = 1;
//        for (int hs = dy; j + h < dz; h++, hs += dy)
//            for (int k = 0; k < w; k++)
//                if (c != m[n + k + hs]) {
//                    return h;
//                }
//        return h;
//    }
//
//    private int determineHeightY(int c, int n, int j, int w) {
//        int h = 1;
//        for (int hs = dz; j + h < dx; h++, hs += dz)
//            for (int k = 0; k < w; k++)
//                if (c != m[n + k + hs]) {
//                    return h;
//                }
//        return h;
//    }
//
//    private int determineHeightZ(int c, int n, int j, int w) {
//        int h = 1;
//        for (int hs = dx; h < MAX_MERGE_LENGTH && j + h < dy; h++, hs += dx)
//            for (int k = 0; k < w; k++)
//                if (c != m[n + k + hs]) {
//                    return h;
//                }
//        return h;
//    }
//}
