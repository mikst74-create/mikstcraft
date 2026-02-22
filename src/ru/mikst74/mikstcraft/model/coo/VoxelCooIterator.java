package ru.mikst74.mikstcraft.model.coo;

public interface VoxelCooIterator {
    boolean iterateU();

    boolean iterateV();

    boolean iterateA();

    int getX();

    int getY();

    int getZ();
}
