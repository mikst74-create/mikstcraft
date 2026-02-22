package ru.mikst74.mikstcraft.model.coo;

import lombok.Getter;
import ru.mikst74.mikstcraft.model.NeighborCode;

import static ru.mikst74.mikstcraft.model.NeighborCode.*;

@Getter
public class VoxelCoo extends GridCoo<VoxelCoo> {
    public static final int CHUNK_SIZE_SHIFT = 4;
    public static final int CHUNK_SIZE       = 16;
    public static final int IDX_MUL          = CHUNK_SIZE + 2;
    public static final int MAX_VOXEL_COO    = CHUNK_SIZE - 1;

    private int sideShift;
    private int sideMax;

    private boolean iteratorX;
    private boolean iteratorY;
    private boolean iteratorZ;

    public VoxelCoo(int x, int y, int z) {
        init();
        assign(x, y, z);
    }

    public VoxelCoo() {
        init();
    }

    public VoxelCoo(VoxelCoo voxelCoo) {
        this(voxelCoo.getX(), voxelCoo.getY(), voxelCoo.getZ());
    }

    protected void init() {
        sideShift = CHUNK_SIZE_SHIFT;
        sideMax   = (1 << sideShift) - 1;

        iteratorX = false;
        iteratorY = false;
        iteratorZ = false;
    }


    public boolean onEdge(NeighborCode nc) {
        return (nc.getDx() < 0 && x == 0) ||
                (nc.getDx() > 0 && x == sideMax) ||
                (nc.getDy() < 0 && y == 0) ||
                (nc.getDy() > 0 && y == sideMax) ||
                (nc.getDz() < 0 && z == 0) ||
                (nc.getDz() > 0 && z == sideMax);
    }


    public int idx() {
        return ((((z + 1) * IDX_MUL) + (y + 1)) * IDX_MUL) + (x + 1);
    }

    public int idx0() {
        return (((z << 4) + y) << 4) + x;
    }

    public boolean isMaxX() {
        return x >= sideMax;
    }

    public boolean isMaxY() {
        return y >= sideMax;
    }

    public boolean isMaxZ() {
        return z >= sideMax;
    }

    public boolean isMinX() {
        return x <= 0;
    }

    public boolean isMinY() {
        return y <= 0;
    }

    public boolean isMinZ() {
        return z <= 0;
    }


    public boolean iterate(int axis) {
        if (axis == CooConstant.X_AXIS) {
            return iterateX();
        }
        if (axis == CooConstant.Y_AXIS) {
            return iterateY();
        }
        if (axis == CooConstant.Z_AXIS) {
            return iterateZ();
        }
        throw new RuntimeException("unknown axis " + axis);
    }

    public boolean iterate(int axis, int min, int max) {
        if (axis == CooConstant.X_AXIS) {
            return iterateX(min, max);
        }
        if (axis == CooConstant.Y_AXIS) {
            return iterateY(min, max);
        }
        if (axis == CooConstant.Z_AXIS) {
            return iterateZ(min, max);
        }
        throw new RuntimeException("unknown axis " + axis);
    }

    public boolean iterateX() {
        if (!iteratorX) {
            x         = 0;
            iteratorX = true;
        } else {
            incX();
            if ((x & sideMax) == 0) {
                iteratorX = false;
                x         = 0;
            }
        }
        return iteratorX;
    }

    public boolean iterateX(int min, int max) {
        if (!iteratorX) {
            x         = min;
            iteratorX = true;
        } else {
            incX();
            if (x > max) {
                iteratorX = false;
                x         = min;
            }
        }
        return iteratorX;
    }

    public boolean iterateY() {
        if (!iteratorY) {
            y         = 0;
            iteratorY = true;
        } else {
            incY();
            if ((y & sideMax) == 0) {
                iteratorY = false;
                y         = 0;
            }
        }
        return iteratorY;
    }

    public boolean iterateY(int min, int max) {
        if (!iteratorY) {
            y         = min;
            iteratorY = true;
        } else {
            incY();
            if (y > max) {
                iteratorY = false;
                y         = min;
            }
        }
        return iteratorY;
    }

    public boolean iterateZ() {
        if (!iteratorZ) {
            z         = 0;
            iteratorZ = true;
        } else {
            incZ();
            if ((z & sideMax) == 0) {
                iteratorZ = false;
                z         = 0;
            }
        }
        return iteratorZ;
    }

    public boolean iterateZ(int min, int max) {
        if (!iteratorZ) {
            z         = min;
            iteratorZ = true;
        } else {
            incZ();
            if (z > max) {
                iteratorZ = false;
                z         = min;
            }
        }
        return iteratorZ;
    }

    public VoxelCoo revert() {
        x = sideMax - x;
        y = sideMax - y;
        z = sideMax - z;
        return this;
    }

    @Override
    public String toString() {
        return "VoxelCoo{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }


    public boolean isMax(int axis) {
        if (axis == CooConstant.X_AXIS) {
            return isMaxX();
        }
        if (axis == CooConstant.Y_AXIS) {
            return isMaxY();
        }
        if (axis == CooConstant.Z_AXIS) {
            return isMaxZ();
        }
        throw new RuntimeException("unknown axis " + axis);
    }

    public NeighborCode getNeighborEdge() {
        return
                x == 0 ? XM :
                        x == sideMax ? XP :
                                y == 0 ? YM :
                                        y == sideMax ? YP :
                                                z == 0 ? ZM :
                                                        z == sideMax ? ZP :
                                                                null;

    }

    public boolean isInnerChunk() {
        return x >= 0 && y >= 0 && z >= 0 && x <= sideMax && y <= sideMax && z <= sideMax;
    }
}

