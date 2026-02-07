package ru.mikst74.mikstcraft.model.coo;

public class VoxelCooIteratorYAxis implements VoxelCooIterator{
    VoxelCoo c;

    public VoxelCooIteratorYAxis(VoxelCoo c) {
        this.c = new VoxelCoo(c);
    }

    @Override
    public boolean iterateU() {
        return c.iterateY();
    }

    @Override
    public boolean iterateV() {
        return c.iterateZ();
    }

    @Override
    public boolean iterateA() {
        return c.iterateX();
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
