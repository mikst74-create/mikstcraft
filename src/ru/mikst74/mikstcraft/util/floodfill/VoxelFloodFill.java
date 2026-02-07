package ru.mikst74.mikstcraft.util.floodfill;
import java.util.LinkedList;
import java.util.Queue;

public class VoxelFloodFill {
    // Helper class to store 3D coordinates
    static class Point3D {
        int x, y, z;
        Point3D(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
    }

    public void floodFill3D(int[][][] grid, int startX, int startY, int startZ, int newFill) {
        int targetValue = grid[startX][startY][startZ];

        // Base case: if start is already the fill value, stop
        if (targetValue == newFill) return;

        int sizeX = grid.length;
        int sizeY = grid[0].length;
        int sizeZ = grid[0][0].length;

        Queue<Point3D> queue = new LinkedList<>();
        queue.add(new Point3D(startX, startY, startZ));

        while (!queue.isEmpty()) {
            Point3D p = queue.poll();

            // Check boundaries
            if (p.x < 0 || p.x >= sizeX || p.y < 0 || p.y >= sizeY || p.z < 0 || p.z >= sizeZ) continue;

            // If the current block matches the target value, fill and add neighbors
            if (grid[p.x][p.y][p.z] == targetValue) {
                grid[p.x][p.y][p.z] = newFill;

                // Add 6 adjacent neighbors (3D)
                queue.add(new Point3D(p.x + 1, p.y, p.z));
                queue.add(new Point3D(p.x - 1, p.y, p.z));
                queue.add(new Point3D(p.x, p.y + 1, p.z));
                queue.add(new Point3D(p.x, p.y - 1, p.z));
                queue.add(new Point3D(p.x, p.y, p.z + 1));
                queue.add(new Point3D(p.x, p.y, p.z - 1));
            }
        }
    }
}