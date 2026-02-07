package ru.mikst74.mikstcraft.meshing;

import lombok.SneakyThrows;
import ru.mikst74.mikstcraft.model.NeighborCode;
import ru.mikst74.mikstcraft.model.chunk.Chunk;
import ru.mikst74.mikstcraft.model.coo.VoxelCoo;

import static ru.mikst74.mikstcraft.model.NeighborCode.*;
import static ru.mikst74.mikstcraft.model.coo.VoxelCoo.MAX_VOXEL_COO;

/**
 * Implementation of Greedy Meshing that takes into account the minimum/maximum Y coordinate of
 * active voxels to speedup the meshing.
 * <p>
 * It also generates "neighbor configurations" for simple AO inside of the face value.
 */
public class GreedyMeshing3 {


//    public static final long SIEVE_MASK = 0x5555555555555555L;
    public static final int SIEVE_MASK = 0x55555555;

    private Chunk  chunk;
    private int[]  count;
    private int[] gluedFaceField;


    public GreedyMeshing3() {
        count = new int[6];
    }

    @SneakyThrows
    public int[] mesh(Chunk chunk, FaceConsumer faces) {
        this.chunk          = chunk;
        this.gluedFaceField = chunk.getVoxelField().getCopyOfGluedFaceField();
        newMeshX(faces);
        newMeshY(faces);
        newMeshZ(faces);
        return count;
    }


    private void newMeshX(FaceConsumer faces) {
        VoxelCoo c = new VoxelCoo();
        NeighborCode nc;

        nc = XP;
        while (c.iterateX()) {
            while (c.iterateY()) {
                int mask = gluedFaceField[(nc.getI() << 8) + (c.getX() << 4) + c.getY()];
                if (mask == 0) {
                    continue;
                }
                mergeAndGenerateFacesX(faces, new VoxelCoo(c), mask, nc);
            }
        }
        nc = XM;
        while (c.iterateX()) {
            while (c.iterateY()) {
                int mask = gluedFaceField[(nc.getI() << 8L) + (c.getX() << 4) + c.getY()];
                if (mask == 0) {
                    continue;
                }
                mergeAndGenerateFacesX(faces, new VoxelCoo(c), mask, nc);
            }
        }
    }

    private void newMeshY(FaceConsumer faces) {
        VoxelCoo c = new VoxelCoo();
        NeighborCode nc;

        nc = YP;
        while (c.iterateY()) {
            while (c.iterateZ()) {
                int mask = gluedFaceField[(nc.getI() << 8) + (c.getY() << 4) + c.getZ()];
                if (mask == 0) {
                    continue;
                }
                mergeAndGenerateFacesY(faces, new VoxelCoo(c), mask, nc);
            }
        }
        nc = YM;
        while (c.iterateY()) {
            while (c.iterateZ()) {
                int mask = gluedFaceField[(nc.getI() << 8) + (c.getY() << 4) + c.getZ()];
                if (mask == 0) {
                    continue;
                }
                mergeAndGenerateFacesY(faces, new VoxelCoo(c), mask, nc);
            }
        }
    }

    private void newMeshZ(FaceConsumer faces) {
        VoxelCoo c = new VoxelCoo();
        NeighborCode nc;

        nc = ZP;
        while (c.iterateZ()) {
            while (c.iterateX()) {
                int mask = gluedFaceField[(nc.getI() << 8) + (c.getZ() << 4) + c.getX()];
                if (mask == 0) {
                    continue;
                }
                mergeAndGenerateFacesZ(faces, new VoxelCoo(c), mask, nc);
            }
        }
        nc = ZM;
        while (c.iterateZ()) {
            while (c.iterateX()) {
                int mask = gluedFaceField[(nc.getI() << 8) + (c.getZ() << 4) + c.getX()];
                if (mask == 0) {
                    continue;
                }
                mergeAndGenerateFacesZ(faces, new VoxelCoo(c), mask, nc);
            }
        }
    }

    private void mergeAndGenerateFacesX(FaceConsumer faces, VoxelCoo startCoo, int mask, NeighborCode nc) {
        // iterate by X, Y
        // mask along Z
        VoxelCoo meshCoo = new VoxelCoo(startCoo);
        while ((mask & SIEVE_MASK) > 0) {
            int skip = skipMaskZeroBits(mask);
            mask = mask >>> skip >>> skip;
            meshCoo.addZ(skip);
            // Get current voxel texture
            int textureId = chunk.getFaceInfo(meshCoo, nc);
            int w = applyOneBitsWithTextureCheck(mask);
            mask = mask >>> w >>> w;
            int h = 1;
            if (!meshCoo.isMaxY()) {
                // try to extend mask to UP
                VoxelCoo hCoo = new VoxelCoo(meshCoo);
                int hMask = ((1 << w << w >>> 1) - 1) << hCoo.getZ() << hCoo.getZ();
                while (hCoo.getY() < MAX_VOXEL_COO) {
                    hCoo.incY();
                    int upRowTextureId = chunk.getFaceInfo(hCoo, nc);
                    int l = gluedFaceField[(nc.getI() << 8) + (hCoo.getX() << 4) + hCoo.getY()];
                    if (textureId != upRowTextureId || (hMask & l) != hMask) {
                        break;
                    }
                    gluedFaceField[(nc.getI() << 8) + (hCoo.getX() << 4) + hCoo.getY()] = l & ~hMask;
                    h++;
                }
            }
            faces.consume(meshCoo.getY(), meshCoo.getZ(),
                    meshCoo.getY() + h, meshCoo.getZ() + w,
                    meshCoo.getX() + nc.getP(), nc, textureId);
            count[nc.getI()]++;
            meshCoo.addZ(w);
        }
    }

    private void mergeAndGenerateFacesY(FaceConsumer faces, VoxelCoo startCoo, int mask, NeighborCode nc) {
        // iterate by Y, Z
        // mask along X
        VoxelCoo meshCoo = new VoxelCoo(startCoo);
        while ((mask & SIEVE_MASK) > 0) {
            int skip = skipMaskZeroBits(mask);
            mask = mask >>> skip >>> skip;
            meshCoo.addX(skip);
            int textureId = chunk.getFaceInfo(meshCoo, nc);
            int w = applyOneBitsWithTextureCheck(mask);
            mask = mask >>> w >>> w;
            int h = 1;
            if (!meshCoo.isMaxZ()) {
                // try to extend mask to UP
                VoxelCoo hCoo = new VoxelCoo(meshCoo);
                int hMask = ((1 << w << w >>> 1) - 1) << hCoo.getX() << hCoo.getX();
                while (hCoo.getZ() < MAX_VOXEL_COO) {
                    hCoo.incZ();
                    int upRowTextureId = chunk.getFaceInfo(hCoo, nc);
                    int l = gluedFaceField[(nc.getI() << 8) + (hCoo.getY() << 4) + hCoo.getZ()];
                    if (textureId != upRowTextureId || (hMask & l) != hMask) {
                        break;
                    }
                    gluedFaceField[(nc.getI() << 8) + (hCoo.getY() << 4) + hCoo.getZ()] = l & ~hMask;
                    h++;
                }
            }
            faces.consume(meshCoo.getZ(), meshCoo.getX(),
                    meshCoo.getZ() + h, meshCoo.getX() + w,
                    meshCoo.getY() + nc.getP(), nc, textureId);
            count[nc.getI()]++;
            meshCoo.addX(w);
        }
    }

    private void mergeAndGenerateFacesZ(FaceConsumer faces, VoxelCoo startCoo, int mask, NeighborCode nc) {
        // iterate by Z, X
        // mask along Y
        VoxelCoo meshCoo = new VoxelCoo(startCoo);
        while ((mask & SIEVE_MASK) > 0) {
            int skip = skipMaskZeroBits(mask);
            mask = mask >>> skip >>> skip;
            meshCoo.addY(skip);
            // Get current voxel texture
            int textureId = chunk.getFaceInfo(meshCoo, nc);
            int w = applyOneBitsWithTextureCheck(mask);
            mask = mask >>> w >>> w;
            int h = 1;
            if (!meshCoo.isMaxX()) {
                // try to extend mask to UP
                VoxelCoo hCoo = new VoxelCoo(meshCoo);
                int hMask = ((1 << w << w >>> 1) - 1) << hCoo.getY() << hCoo.getY();
                while (hCoo.getX() < MAX_VOXEL_COO) {
                    hCoo.incX();
                    int upRowTextureId = chunk.getFaceInfo(hCoo, nc);
                    int l = gluedFaceField[(nc.getI() << 8) + (hCoo.getZ() << 4) + hCoo.getX()];
//                System.out.println(format64BitLongAs64String(hMask) + " <- hMask");
//                System.out.println(format64BitLongAs64String(l) + " <- l");
//                System.out.println(format64BitLongAs64String(hMask & l) + " <- hMask & l");
//                System.out.println("");
                    if (textureId != upRowTextureId || (hMask & l) != hMask) {
                        break;
                    }
                    gluedFaceField[(nc.getI() << 8) + (hCoo.getZ() << 4) + hCoo.getX()] = l & ~hMask;
                    h++;
                }
            }
            faces.consume(meshCoo.getX(), meshCoo.getY(),
                    meshCoo.getX() + h, meshCoo.getY() + w,
                    meshCoo.getZ() + nc.getP(), nc, textureId);
            count[nc.getI()]++;
            meshCoo.addY(w);
        }
    }

    private int applyOneBitsWithTextureCheck(int mask) {
        int shift = 0;
        while ((mask & 3) == 3) {
            mask = mask >>> 2;
            shift++;
        }
        if ((mask & 3) == 1) {
            shift++;
        }
        return shift;
    }

    private static int skipMaskZeroBits(int mask) {
        // Skip all zero bits in mask
        int shift = 0;
        while ((mask & 1) == 0) {
            mask = mask >>> 2;
            shift++;
        }
        return shift;
    }

}
