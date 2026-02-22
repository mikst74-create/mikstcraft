package ru.mikst74.mikstcraft.model.coo;

public class VoxelCooIteratorZAxis implements VoxelCooIterator {
    VoxelCoo c;

    public VoxelCooIteratorZAxis(VoxelCoo c) {
        this.c = new VoxelCoo(c);
    }

    @Override
    public boolean iterateU() {
        return c.iterateZ();
    }

    @Override
    public boolean iterateV() {
        return c.iterateX();
    }

    @Override
    public boolean iterateA() {
        return c.iterateY();
    }

    @Override
    public int getX() {
        return c.getX();
    }

    @Override
    public int getY() {
        return c.getY();
    }

    @Override
    public int getZ() {
        return c.getZ();
    }
}
