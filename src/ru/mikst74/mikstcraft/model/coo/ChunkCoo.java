package ru.mikst74.mikstcraft.model.coo;

public class ChunkCoo extends GridCoo<ChunkCoo> {
    public ChunkCoo() {
        super();
    }

    public ChunkCoo(int x, int y, int z) {
        super(x, y, z);
    }

    public ChunkCoo(ChunkCoo coo) {
        super(coo);
    }

    @Override
    public String toString() {
        return "ChunkCoo{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public ChunkCoo T(){
        return this;
    }

}
