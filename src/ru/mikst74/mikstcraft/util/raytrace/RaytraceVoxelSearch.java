package ru.mikst74.mikstcraft.util.raytrace;

import org.joml.Vector3d;
import org.joml.Vector3f;
import ru.mikst74.mikstcraft.dictionary.BlockTypeInfo;
import ru.mikst74.mikstcraft.model.NeighborCode;
import ru.mikst74.mikstcraft.model.coo.WorldCoo;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.lang.Math.*;
import static ru.mikst74.mikstcraft.dictionary.BlockTypeDictionary.AIR_BLOCK;
import static ru.mikst74.mikstcraft.model.NeighborCode.getNeighborCodeBySideOffset;

public class RaytraceVoxelSearch {

    public static final int MAX_STEPS = 16;

    /**
     * Determine the voxel pointed to by a ray <code>(ox, oy, oz) + t * (dx, dy, dz)</code> and store
     * the position and side offset of that voxel (if any) into {  selectedVoxelPosition} and
     * {  sideOffset}, respectively.
     *
     * @param original  the ray origin's coordinates
     * @param direction the ray direction's  coordinates
     */
    public static boolean findAndSetSelectedVoxel(Vector3d original,
                                                  Vector3f direction,
                                                  Function<WorldCoo, Integer> load, // load voxel by worldCoo
                                                  BiConsumer<WorldCoo, NeighborCode> finded) // return finded voxel in worldCoo + sideCoo
    {
        /* "A Fast Voxel Traversal Algorithm for Ray Tracing" by John Amanatides, Andrew Woo */
//        System.out.println("start findAndSetSelectedVoxel from "+original+" to "+direction);
        float big = 1E30f;
        int
                ox = (int) floor(original.x),
                oy = (int) floor(original.y),
                oz = (int) floor(original.z);
        WorldCoo p = new WorldCoo(ox, oy, oz);
        float
                dxi = 1f / direction.x,
                dyi = 1f / direction.y,
                dzi = 1f / direction.z;
        int
                sx = direction.x > 0 ? 1 : -1,
                sy = direction.y > 0 ? 1 : -1,
                sz = direction.z > 0 ? 1 : -1;
        float
                dtx = min(dxi * sx, big),
                dty = min(dyi * sy, big),
                dtz = min(dzi * sz, big);
        float
                tx = abs((p.getX() + max(sx, 0) - ox) * dxi),
                ty = abs((p.getY() + max(sy, 0) - oy) * dyi),
                tz = abs((p.getZ() + max(sz, 0) - oz) * dzi);
        int
                cmpx = 0,
                cmpy = 0,
                cmpz = 0;
        for (int i = 0; i < MAX_STEPS; i++) {
            Integer voxel = load.apply(p);
//            System.out.println("load at "+p+" = "+voxel);
            if (voxel != 0) {
                finded.accept(p, getNeighborCodeBySideOffset(-cmpx * sx, -cmpy * sy, -cmpz * sz));
                return true;
            }
            /* Advance to next voxel */
            cmpx = step(tx, tz) * step(tx, ty);
            cmpy = step(ty, tx) * step(ty, tz);
            cmpz = step(tz, ty) * step(tz, tx);
            tx += dtx * cmpx;
            ty += dty * cmpy;
            tz += dtz * cmpz;
            p.addX(sx * cmpx);
            p.addY(sy * cmpy);
            p.addZ(sz * cmpz);
        }
        return false;
    }

    /**
     * GLSL's step function.
     */
    private static int step(float edge, float v) {
        return v < edge ? 0 : 1;
    }


    public  static boolean findAndSetSelectedVoxelOld(float ox, float oy, float oz, float dx, float dy, float dz,Function<WorldCoo, Integer> load, // load voxel by worldCoo
                                                   BiConsumer<WorldCoo, NeighborCode> finded) {
        /* "A Fast Voxel Traversal Algorithm for Ray Tracing" by John Amanatides, Andrew Woo */
        float big = 1E30f;
        int px = (int) floor(ox), py = (int) floor(oy), pz = (int) floor(oz);
        float dxi = 1f / dx, dyi = 1f / dy, dzi = 1f / dz;
        int sx = dx > 0 ? 1 : -1, sy = dy > 0 ? 1 : -1, sz = dz > 0 ? 1 : -1;
        float dtx = min(dxi * sx, big), dty = min(dyi * sy, big), dtz = min(dzi * sz, big);
        float tx = abs((px + max(sx, 0) - ox) * dxi), ty = abs((py + max(sy, 0) - oy) * dyi), tz = abs((pz + max(sz, 0) - oz) * dzi);
        int maxSteps = 16;
        int cmpx = 0, cmpy = 0, cmpz = 0;
        boolean hasSelection;
        for (int i = 0; i < maxSteps  ; i++) {
            if (i > 0  ) {
                if (load.apply(new WorldCoo(px, py, pz)) != 0) {
                    finded.accept(new WorldCoo(px, py, pz),
                            getNeighborCodeBySideOffset(-cmpx * sx, -cmpy * sy, -cmpz * sz));
//                    selectedVoxelPosition.set(px, py, pz);
//                    sideOffset.set(-cmpx * sx, -cmpy * sy, -cmpz * sz);
                    hasSelection = true;
//                    return true;
                }
            }
            /* Advance to next voxel */
            cmpx = step(tx, tz) * step(tx, ty);
            cmpy = step(ty, tx) * step(ty, tz);
            cmpz = step(tz, ty) * step(tz, tx);
            tx += dtx * cmpx;
            ty += dty * cmpy;
            tz += dtz * cmpz;
            px += sx * cmpx;
            py += sy * cmpy;
            pz += sz * cmpz;
        }
        hasSelection = false;
        return hasSelection;
    }


    /**
     *  version from Google AI - best
     * @param rayOrigin
     * @param rayDir
     * @param load
     * @param finded
     * @return
     */
    public static boolean traverseFromAI(Vector3f rayOrigin, Vector3f rayDir, Function<WorldCoo, BlockTypeInfo> load, // load voxel by worldCoo
                                         BiConsumer<WorldCoo, NeighborCode> finded) {
        float maxDistance=24f;
        // 1. Initialize Current Voxel
        int x = (int) Math.floor(rayOrigin.x);
        int y = (int) Math.floor(rayOrigin.y);
        int z = (int) Math.floor(rayOrigin.z);

        // 2. Determine step direction (1 or -1)
        int stepX = (rayDir.x > 0) ? 1 : (rayDir.x < 0 ? -1 : 0);
        int stepY = (rayDir.y > 0) ? 1 : (rayDir.y < 0 ? -1 : 0);
        int stepZ = (rayDir.z > 0) ? 1 : (rayDir.z < 0 ? -1 : 0);

        // 3. Calculate tDelta (distance to travel one full voxel width)
        // Using Float.MAX_VALUE for zero-direction components to avoid division by zero
        float tDeltaX = (stepX != 0) ? Math.abs(1.0f / rayDir.x) : Float.MAX_VALUE;
        float tDeltaY = (stepY != 0) ? Math.abs(1.0f / rayDir.y) : Float.MAX_VALUE;
        float tDeltaZ = (stepZ != 0) ? Math.abs(1.0f / rayDir.z) : Float.MAX_VALUE;

        // 4. Calculate tMax (distance to the first voxel boundary)
        float tMaxX = (float) ((stepX > 0) ? (Math.floor(rayOrigin.x) + 1 - rayOrigin.x) * tDeltaX :
                        (stepX < 0) ? (rayOrigin.x - Math.floor(rayOrigin.x)) * tDeltaX : Float.MAX_VALUE);
        float tMaxY = (float) ((stepY > 0) ? (Math.floor(rayOrigin.y) + 1 - rayOrigin.y) * tDeltaY :
                        (stepY < 0) ? (rayOrigin.y - Math.floor(rayOrigin.y)) * tDeltaY : Float.MAX_VALUE);
        float tMaxZ = (float) ((stepZ > 0) ? (Math.floor(rayOrigin.z) + 1 - rayOrigin.z) * tDeltaZ :
                        (stepZ < 0) ? (rayOrigin.z - Math.floor(rayOrigin.z)) * tDeltaZ : Float.MAX_VALUE);

        // 5. The Traversal Loop
        float distance = 0;
        int side=0;

        while (distance < maxDistance) {
            // Check the current voxel (x, y, z)
            BlockTypeInfo blockTypeInfo = load.apply(new WorldCoo(x, y, z));
            if (blockTypeInfo != AIR_BLOCK) {
//                System.out.println("Hit at: " + x + ", " + y + ", " + z);
                Vector3f normal = new Vector3f(0, 0, 0);
                if (side == 0) normal.x = -stepX; // Normal points opposite of step direction
                else if (side == 1) normal.y = -stepY;
                else if (side == 2) normal.z = -stepZ;
                finded.accept(new WorldCoo(x, y, z),
                      getNeighborCodeBySideOffset((int) normal.x, (int) normal.y, (int) normal.z) );
                    return true;
            }

            // Step to the next voxel boundary
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    distance = tMaxX;
                    tMaxX += tDeltaX;
                    x += stepX;
                     side = 0;
                } else {
                    distance = tMaxZ;
                    tMaxZ += tDeltaZ;
                    z += stepZ;
                    side = 2;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    distance = tMaxY;
                    tMaxY += tDeltaY;
                    y += stepY;
                    side = 1;
                } else {
                    distance = tMaxZ;
                    tMaxZ += tDeltaZ;
                    z += stepZ;
                    side = 2;
                }
            }
        }
        return false;
    }

}
