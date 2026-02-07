package ru.mikst74.mikstcraft.model;

import lombok.Getter;

import java.util.function.Consumer;

@Getter
public enum NeighborCode {
    XP(0, 1, 1, 0, 1, 1, 0, 0, 1, 0, 0, 1, 1),
    XM(1, 2, -1, 0, 0, -1, 0, 0, 1, 0, 0, 0, 1),
    YP(2, 4, 1, 1, 1, 0, 1, 0, 0, 1, 0, 3, 2),
    YM(3, 8, -1, 1, 0, 0, -1, 0, 0, 1, 0, 2, 2),
    ZP(4, 16, 1, 2, 1, 0, 0, 1, 0, 0, 1, 5, 4),
    ZM(5, 32, -1, 2, 0, 0, 0, -1, 0, 0, 1, 4, 4);

//    XPYP(6, 0, 1, 1, 1, 0, 0, 0, 0),
//    XPYM(6, 0, 1, 1, 1, 0, 0, 0, 0),
//    XPZP(6, 0, 1, 1, 1, 0, 0, 0, 0),
//    XPZM(6, 0, 1, 1, 1, 0, 0, 0, 0),
//    XMYP(6, 0, 1, 1, 1, 0, 0, 0, 0),
//    XMYM(6, 0, 1, 1, 1, 0, 0, 0, 0),
//    XMZP(6, 0, 1, 1, 1, 0, 0, 0, 0),
//    XMZM(6, 0, 1, 1, 1, 0, 0, 0, 0),
//    ;

    public static final int XPi = XP.i;
    public static final int XMi = XM.i;
    public static final int YPi = YP.i;
    public static final int YMi = YM.i;
    public static final int ZPi = ZP.i;
    public static final int ZMi = ZM.i;

    public static final int XPb = XP.b;
    public static final int XMb = XM.b;
    public static final int YPb = YP.b;
    public static final int YMb = YM.b;
    public static final int ZPb = ZP.b;
    public static final int ZMb = ZM.b;

    private final int i; // index for arrays
    private final int b; // bit mask
    private final int d; // direction +1 or -1 (by any axis)
    private final int a; // axis code (0-X, 1-Y, 2-Z)
    private final int p; // is positive direction (0/1)
    private final int dx; // direction by axis X
    private final int dy; // direction by axis Y
    private final int dz; // direction by axis Z
    private final int ex; // flag is than NeighborCode along X axis
    private final int ey; // flag is than NeighborCode along Y axis
    private final int ez; // flag is than NeighborCode along Z axis


    private final int ms; //mesher side
    private final int cvd; // ChunkVisibility direction
    private NeighborCode opposite;

    static {
        XP.opposite = XM;
        XM.opposite = XP;
        YP.opposite = YM;
        YM.opposite = YP;
        ZP.opposite = ZM;
        ZM.opposite = ZP;
    }

    NeighborCode(int i, int b, int d, int a, int p, int dx, int dy, int dz, int ex, int ey, int ez, int ms, int cvd) {
        this.i   = i;
        this.b   = b;
        this.d   = d;
        this.a   = a;
        this.p   = p;
        this.dx  = dx;
        this.dy  = dy;
        this.dz  = dz;
        this.ex  = ex;
        this.ey  = ey;
        this.ez  = ez;
        this.ms  = ms;
        this.cvd = cvd;
    }

    public static NeighborCode getNeighborCodeBySideOffset(int dx, int dy, int dz) {
        for (NeighborCode nc : NeighborCode.values()) {
            if (nc.dx == dx && nc.dy == dy && nc.dz == dz) return nc;
        }
        return null;
//        throw new RuntimeException("Unknown NeighborCode offset: dx,dy,dz:" + dx + "," + dy + "," + dz);
    }

    public static void forEachNeighborCode(Consumer<NeighborCode> func) {
        for (NeighborCode nc : NeighborCode.values()) {
            func.accept(nc);
        }
    }

    @Override
    public String toString() {
        return "NC=" + name();
    }
}
