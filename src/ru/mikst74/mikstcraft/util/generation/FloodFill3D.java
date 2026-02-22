package ru.mikst74.mikstcraft.util.generation;

public class FloodFill3D {
    private static final int     MARKED_BLOCK = 0xFF00;
    // 6-connected neighbors (4-way in 2D + up/down)
    private static final int[][] DIRECTIONS   = {
            {1, 0, 0, 1}, {-1, 0, 0, 1}, {0, 1, 0, 2},
            {0, -1, 0, 2}, {0, 0, 1, 4}, {0, 0, -1, 4}
    };

    public int[][][] floodFill(int[][][] cube, int x, int y, int z, int d) {
        if ((cube[x][y][z] & (MARKED_BLOCK | d)) != 0) {
            fill(cube, x, y, z, 0);
        }
        return cube;
    }

    private void fill(int[][][] cube, int x, int y, int z, int d) {
        // d  - direction mask
        int depth = cube.length;
        int height = cube[0].length;
        int width = cube[0][0].length;

        // Boundary check and color check
        if (x < 0 || x >= depth || y < 0 || y >= height || z < 0 || z >= width
                || (cube[x][y][z] & (MARKED_BLOCK | d)) != 0) {
            return;
        }

        // Replace color
        cube[x][y][z] |= MARKED_BLOCK;

        // Recursively fill neighbors
        for (int[] dir : DIRECTIONS) {
            fill(cube, x + dir[0], y + dir[1], z + dir[2], dir[3]);
        }
    }
}
