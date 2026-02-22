package ru.mikst74.mikstcraft.model.coo;

import lombok.Getter;
import org.joml.Vector3f;

@Getter
public class WCVConverter {
    public static WCVConverter staticWCV = new WCVConverter();
    private       WorldCoo     w;
    private       ChunkCoo     c;
    private       VoxelCoo     v;

    public WCVConverter() {
        w = new WorldCoo();
        c = new ChunkCoo();
        v = new VoxelCoo();
    }

    public WCVConverter(WorldCoo w) {
        this();
        assign(w);
    }

    public WCVConverter(ChunkCoo c) {
        this();
        assign(c);
    }


    public WCVConverter(Vector3f worldFloatPosition) {
        this();
        assign(worldFloatPosition);
    }

    public void assign(Vector3f worldFloatPosition) {
        w.assign(worldFloatPosition);
        assign(w);
    }

    public void assign(WorldCoo wCoo) {
        w.assign(wCoo);
        int shift = v.getSideShift();
        c.setX(w.getX() >> shift);
        c.setY(w.getY() >> shift);
        c.setZ(w.getZ() >> shift);
        v.assign(w.getX() & 0xF, w.getY() & 0xF, w.getZ() & 0xF);
//        v.addX(-(c.getX() << shift));
//        v.addY(-(c.getY() << shift));
//        v.addZ(-(c.getZ() << shift));
//        System.out.println("convert W:" + wCoo + " -> V:" + v);
    }

    public void assign(ChunkCoo cCoo) {
        c.assign(cCoo);
        int shift = v.getSideShift();
        w.setX(c.getX() << shift);
        w.setY(c.getY() << shift);
        w.setZ(c.getZ() << shift);
        v.setX(0);
        v.setY(0);
        v.setZ(0);
    }

}
