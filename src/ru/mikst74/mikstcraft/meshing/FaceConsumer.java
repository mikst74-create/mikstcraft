package ru.mikst74.mikstcraft.meshing;

import ru.mikst74.mikstcraft.model.NeighborCode;

/**
 * Consumes a generated face.
 */
public interface FaceConsumer {
    /**
     * @param u0 the U coordinate of the minimum corner
     * @param v0 the V coordinate of the minimum corner
     * @param u1 the U coordinate of the maximum corner
     * @param v1 the V coordinate of the maximum corner
     * @param p  the main coordinate of the face (depending on the side)
     * @param nc the side of the face (including positive or negative)
     * @param v  the face value (includes neighbor configuration)
     */
    void consume(int u0, int v0, int u1, int v1, int p, NeighborCode nc, int v);
}
