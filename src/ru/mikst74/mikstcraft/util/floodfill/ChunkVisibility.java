package ru.mikst74.mikstcraft.util.floodfill;

import lombok.Getter;
import ru.mikst74.mikstcraft.model.NeighborCode;

import java.util.concurrent.atomic.AtomicBoolean;

import static ru.mikst74.mikstcraft.model.NeighborCode.*;

public class ChunkVisibility {
    private static final int VISITED_BLOCK = 0xC0;
    private static final int EMPTY_BLOCK   = 0;
    public static final  int GRID_SIZE     = 16; // max - 32
    public static final  int MAX_SIZE      = GRID_SIZE - 1; // max - 32

    //    private int              airValue = 0;
    public  boolean          isFinished;
    private VisibilityResult result;

    @Getter
    public static class VisibilityResult {
        // bitmask: 0:-X, 1:+X, 2:-Y, 3:+Y, 4:-Z, 5:+Z
//        private int sidesTouchedMask = 0;

        // 6x6 matrix: is side I connected to side J?
        public boolean[][] matrix = new boolean[6][6];

        public boolean hasExit(NeighborCode exitNc) {
            AtomicBoolean r = new AtomicBoolean(false);
            forEachNeighborCode(nc -> r.set(matrix[nc.getI()][exitNc.getI()] || r.get()));
            return r.get();
        }

        public void setToTrue() {
            forEachNeighborCode(ncFrom ->
                    forEachNeighborCode(ncTo ->
                            matrix[ncFrom.getI()][ncTo.getI()] = true));

        }

        public boolean check(NeighborCode enter, NeighborCode exit) {
            return matrix[enter.getI()][exit.getI()];
        }

        public void assign(VisibilityResult visibilityResult) {
            forEachNeighborCode(ncFrom ->
                    forEachNeighborCode(ncTo ->
                            matrix[ncFrom.getI()][ncTo.getI()] = visibilityResult.getMatrix()[ncFrom.getI()][ncTo.getI()]));
        }
    }

    public ChunkVisibility() {
        result     = new VisibilityResult();
        isFinished = false;
    }

    public VisibilityResult calculateFull(int[][][] grid) {
        isFinished = false;

        for (int u = 0; u < GRID_SIZE; u++) {
            for (int v = 0; v < GRID_SIZE; v++) {
                // calculate  each egdes entire 16x16
                // inside calculate function has prevented return
                calculate(grid, 0, u, v, XP);
                calculate(grid, GRID_SIZE - 1, u, v, XM);
                calculate(grid, u, 0, v, YP);
                calculate(grid, u, GRID_SIZE - 1, v, YM);
                calculate(grid, u, v, 0, ZP);
                calculate(grid, u, v, GRID_SIZE - 1, ZM);
                if (isFinished) {
                    return result;
                }
            }
        }
        return result;

    }

    private void calculate(int[][][] grid, int startX, int startY, int startZ, NeighborCode d) {
        if (isFinished) {
            return;
        }
        if ((grid[startX][startY][startZ] & (VISITED_BLOCK | d.getCvd())) != EMPTY_BLOCK) {
            return;
        }

        int[] stack = new int[GRID_SIZE * GRID_SIZE * GRID_SIZE]; // Max voxels in 32^3
        int top = -1;

        // Use a temp value or a 'visited' array to avoid modifying your world data permanently
        // Here we use a unique 'visited' marker (e.g., -1)
        grid[startX][startY][startZ] |= VISITED_BLOCK;
        stack[++top] = (startX << 8) | (startY << 4) | startZ;
        int sidesTouchedMask = 0;
        while (top >= 0) {
            int p = stack[top--];
            // unpack coo from int
            int x = (p >> 8) & 0x0F;
            int y = (p >> 4) & 0x0F;
            int z = p & 0x0F;

            // Check boundaries to mark which faces are touched
            if (x == 0) {
                sidesTouchedMask |= (XMb );
            }
            if (x == MAX_SIZE) {
                sidesTouchedMask |= (XPb );
            }
            if (y == 0) {
                sidesTouchedMask |= (YMb );
            }
            if (y == MAX_SIZE) {
                sidesTouchedMask |= (YPb );
            }
            if (z == 0) {
                sidesTouchedMask |= (ZMb );
            }
            if (z == MAX_SIZE) {
                sidesTouchedMask |= (ZPb );
            }

            // Push neighbors and mark immediately
            top = push(grid, x + 1, y, z, XP.getCvd(), stack, top);
            top = push(grid, x - 1, y, z, XM.getCvd(), stack, top);
            top = push(grid, x, y + 1, z, YP.getCvd(), stack, top);
            top = push(grid, x, y - 1, z, YM.getCvd(), stack, top);
            top = push(grid, x, y, z + 1, ZP.getCvd(), stack, top);
            top = push(grid, x, y, z - 1, ZM.getCvd(), stack, top);
        }

        // Fill the 6x6 matrix based on the bitmask
        AtomicBoolean matrixFull = new AtomicBoolean(true);
        int finalSidesTouchedMask = sidesTouchedMask;
        forEachNeighborCode((ncFrom) -> {
            forEachNeighborCode((ncTo) -> {
                if ((finalSidesTouchedMask & ncFrom.getB()) != 0) {
                    if ((finalSidesTouchedMask & ncTo.getB()) != 0) {
                        result.matrix[ncFrom.getI()][ncTo.getI()] = true;
                    }
                }
                matrixFull.set(matrixFull.get() && result.matrix[ncFrom.getI()][ncTo.getI()]);
            });
        });
        if (sidesTouchedMask == 0x3F || matrixFull.get()) {
            isFinished = true;
        }

    }

    private int push(int[][][] g, int x, int y, int z, int d, int[] s, int top) {
        if (x >= 0 && x < GRID_SIZE &&
                y >= 0 && y < GRID_SIZE &&
                z >= 0 && z < GRID_SIZE
                && (g[x][y][z] & (VISITED_BLOCK | d)) == EMPTY_BLOCK // flood can be leaked to this voxel in D direction and this voxel not be visited yet
        ) {
            g[x][y][z] |= VISITED_BLOCK; // Mark as visited
            s[++top] = (x << 8) | (y << 4) | z;
        }
        return top;
    }
}