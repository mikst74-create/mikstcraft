package ru.mikst74.mikstcraft.model.coo;

import org.joml.Vector3f;
import org.joml.Vector3i;

import static java.lang.Math.floor;

public class WorldCoo extends GridCoo<WorldCoo> {


    public WorldCoo() {
        super();
    }

    public WorldCoo(int x, int y, int z) {
        super(x, y, z);
    }

    public WorldCoo(WorldCoo wCoo) {
        super(wCoo);
    }

    public WorldCoo(Vector3f v) {
        super();
        assign(v);
    }


    public void assign(Vector3f v) {
        this.x = (int) floor(v.x);
        this.y = (int) floor(v.y);
        this.z = (int) floor(v.z);
    }


    public void assign(Vector3i v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    @Override
    public String toString() {
        return "WorldCoo{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }


}
