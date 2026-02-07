//package org.lwjgl.demo.mikstcraft.world.generator;
//
//import org.lwjgl.demo.mikstcraft.settings.GameProperties;
//import org.lwjgl.demo.mikstcraft.model.chunk.VoxelField;
//
//import static java.lang.Math.max;
//import static java.lang.Math.min;
//import static org.joml.SimplexNoise.noise;
//
//public class NoiseTerrainGenerator implements WorldMapGenerator {
//    /**
//     * Create a voxel field for a chunk at the given chunk position.
//     *
//     * @param cx the x coordinate of the chunk position (in whole chunks)
//     * @param cz the z coordinate of the chunk position (in whole chunks)
//     */
//    public VoxelField createVoxelField(int cx, int cz) {
//        int gx = (cx << GameProperties.CHUNK_SIZE_SHIFT) + GameProperties.GLOBAL_X, gz = (cz << GameProperties.CHUNK_SIZE_SHIFT) + GameProperties.GLOBAL_Z;
//        byte[] field = new byte[(GameProperties.CHUNK_SIZE + 2) * (GameProperties.CHUNK_HEIGHT + 2) * (GameProperties.CHUNK_SIZE + 2)];
//        int maxY = Integer.MIN_VALUE, minY = Integer.MAX_VALUE;
//        int num = 0;
//        for (int z = -1; z < GameProperties.CHUNK_SIZE + 1; z++) {
//            for (int x = -1; x < GameProperties.CHUNK_SIZE + 1; x++) {
////        for (int z = 0; z < GameProperties.CHUNK_SIZE; z++) {
////            for (int x = 0; x < GameProperties.CHUNK_SIZE; x++) {
//                int y = (int) terrainNoise(gx + x, gz + z);
//                y = Math.min(max(y, 0), GameProperties.CHUNK_HEIGHT - 1);
//                maxY = max(maxY, y);
//                minY = min(minY, y);
////                for (int y0 = -1; y0 <= y; y0++) {
//                for (int y0 = 0; y0 <= y; y0++) {
//                    field[VoxelField.idx(x, y0, z)] = (byte) 1;//(y0 == y ? 1 : 2);
////                    field[VoxelField.idx(x, y0, z)] = (byte) (y0 == y ? Math.ceil(Math.random()*7) : 2);
//                    num++;
//                }
//            }
//        }
//        VoxelField res = new VoxelField();
//        res.ny = minY;
//        res.py = maxY;
//        res.num = num;
//        res.field = field;
//        return res;
//    }
//
//    /**
//     * Evaluate a heightmap/terrain noise function at the given global <code>(x, z)</code> position.
//     */
//    private static float terrainNoise(int x, int z) {
//        float xzScale = 0.006f;//0.0056f;//0.0018f;
//        float ampl = 135;//255;
//        float y = 0;
//        float groundLevel = GameProperties.BASE_Y + noise(x * xzScale, z * xzScale) * ampl * 0.1f;
//        for (int i = 0; i < 4; i++) {
//            y += ampl * (noise(x * xzScale, z * xzScale) * 0.5f + 0.2f);
//            ampl *= 0.42f;
//            xzScale *= 2.2f;
//        }
//        y = min(GameProperties.CHUNK_HEIGHT - 2, max(y, groundLevel));
//        return y;
//    }
//}
