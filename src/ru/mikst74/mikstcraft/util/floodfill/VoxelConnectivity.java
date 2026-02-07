package ru.mikst74.mikstcraft.util.floodfill;

public class VoxelConnectivity {
    public static class ConnectivityResult {
        public  int         blocksFilled     = 0;
        public  boolean[][] sideConnectivity = new boolean[6][6];
        private boolean[]   sidesTouched     = new boolean[6];
        // Indices: 0:-X, 1:+X, 2:-Y, 3:+Y, 4:-Z, 5:+Z
    }

    public static ConnectivityResult floodFill3D(int[][][] grid, int startX, int startY, int startZ, int newFill) {
        int targetVal = grid[startX][startY][startZ];
        ConnectivityResult res = new ConnectivityResult();
        if (targetVal == newFill) {
            return res;
        }

        // Stack size is exactly 32^3. Marking on push prevents overflow.
        int[] stack = new int[32768];
        int top = -1;

        // Push start and mark immediately
        grid[startX][startY][startZ] = newFill;
        stack[++top]                 = (startX << 10) | (startY << 5) | startZ;

        while (top >= 0) {
            int encoded = stack[top--];
            int x = (encoded >> 10) & 0x1F;
            int y = (encoded >> 5) & 0x1F;
            int z = encoded & 0x1F;

            res.blocksFilled++;

            // Boundary connectivity check
            if (x == 0) {
                res.sidesTouched[0] = true;
            }
            if (x == 31) {
                res.sidesTouched[1] = true;
            }
            if (y == 0) {
                res.sidesTouched[2] = true;
            }
            if (y == 31) {
                res.sidesTouched[3] = true;
            }
            if (z == 0) {
                res.sidesTouched[4] = true;
            }
            if (z == 31) {
                res.sidesTouched[5] = true;
            }

            // Check and Mark Neighbors
            if (x > 0) {
                tryPush(grid, x - 1, y, z, targetVal, newFill, stack, ++top);
            } else {
                top--; // Backtrack if skip
            }

            // Refactored for safety: Standard 6-way expansion
            top = tryPush(grid, x + 1, y, z, targetVal, newFill, stack, top);
            top = tryPush(grid, x - 1, y, z, targetVal, newFill, stack, top);
            top = tryPush(grid, x, y + 1, z, targetVal, newFill, stack, top);
            top = tryPush(grid, x, y - 1, z, targetVal, newFill, stack, top);
            top = tryPush(grid, x, y, z + 1, targetVal, newFill, stack, top);
            top = tryPush(grid, x, y, z - 1, targetVal, newFill, stack, top);
        }

        // Connect all sides that were part of this contiguous fill
        for (int i = 0; i < 6; i++) {
            if (res.sidesTouched[i]) {
                for (int j = 0; j < 6; j++) {
                    if (res.sidesTouched[j]) {
                        res.sideConnectivity[i][j] = true;
                    }
                }
            }
        }
        return res;
    }

    private static int tryPush(int[][][] g, int x, int y, int z, int target, int fill, int[] s, int top) {
        if (x >= 0 && x < 32 && y >= 0 && y < 32 && z >= 0 && z < 32 && g[x][y][z] == target) {
            g[x][y][z] = fill; // Mark here to prevent duplicate stack entries
            s[++top]   = (x << 10) | (y << 5) | z;
        }
        return top;
    }
}