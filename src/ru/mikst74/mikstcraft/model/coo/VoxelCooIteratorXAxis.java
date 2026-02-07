package ru.mikst74.mikstcraft.model.coo;

import lombok.Getter;

public class VoxelCooIteratorXAxis implements VoxelCooIterator{
    @Getter
    VoxelCoo c;

    public VoxelCooIteratorXAxis() {
        this.c = new VoxelCoo();
    }

    @Override
    public boolean iterateU() {
        return c.iterateX();
    }

    @Override
    public boolean iterateV() {
        return c.iterateY();
    }

    @Override
    public boolean iterateA() {
        return c.iterateZ();
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
