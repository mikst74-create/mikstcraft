package ru.mikst74.mikstcraft.render.model;

import org.joml.FrustumIntersection;

public class FrustumPlanes {
    public float nxX, nxY, nxZ, nxW, pxX, pxY, pxZ, pxW, nyX, nyY, nyZ, nyW, pyX, pyY, pyZ, pyW;
    /**
     * Test whether the box <code>(minX, minY, minZ)</code> - <code>(maxX, maxY, maxZ)</code> is culled
     * by either of the four X, Y planes of the current view frustum.
     * <p>
     * We don't test for near/far planes.
     */
    FrustumIntersection fi;
    public boolean culledXY(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return nxX * (nxX < 0 ? minX : maxX) + nxY * (nxY < 0 ? minY : maxY) + nxZ * (nxZ < 0 ? minZ : maxZ) < -nxW
                || pxX * (pxX < 0 ? minX : maxX) + pxY * (pxY < 0 ? minY : maxY) + pxZ * (pxZ < 0 ? minZ : maxZ) < -pxW
                || nyX * (nyX < 0 ? minX : maxX) + nyY * (nyY < 0 ? minY : maxY) + nyZ * (nyZ < 0 ? minZ : maxZ) < -nyW
                || pyX * (pyX < 0 ? minX : maxX) + pyY * (pyY < 0 ? minY : maxY) + pyZ * (pyZ < 0 ? minZ : maxZ) < -pyW;
    }
}
