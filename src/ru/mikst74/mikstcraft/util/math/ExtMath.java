package ru.mikst74.mikstcraft.util.math;

import org.joml.Vector3f;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ExtMath {

    public static boolean isZeroVector(Vector3f v) {
        return v.x == 0 && v.y == 0 && v.z == 0;
    }

    public static int min5(int v1, int v2, int v3, int v4, int v5) {
        return min(v1, min(v2, min(v3, min(v4, v5))));
    }

    public static int max5(int v1, int v2, int v3, int v4, int v5) {


        return max(v1, max(v2, max(v3, max(v4, v5))));
    }

    /**
     * Round the <em>positive</em> number <code>num</code> up to be a multiple of <code>factor</code>.
     */
    public static int roundUpToNextMultiple(int num, int factor) {
        return num + factor - 1 - (num + factor - 1) % factor;
    }

    /**
     * Задваивает каждый бит в начальном значении.
     *
     * @param r
     * @return
     */
    public static long doubleBits(int v) {
//        public static final long[] DOUBLE_BITS_MAGIC_ARRAY = new long[]{
//                0x55555555,
//                0x33333333,
//                0x0F0F0F0F,
//                0x00FF00FF,
//                0x0000FFFF
//        };
        long highBit = v & 0x80000000;
        highBit = (highBit | (highBit >>> 1)) << 32;

        long r = v & 0x7FFFFFFF; //  fix bug when HI bit is set

        r = (r | (r << 16)) & 0x0000FFFF0000FFFFL;
        r = (r | (r << 8)) & 0x00FF00FF00FF00FFL;
        r = (r | (r << 4)) & 0x0F0F0F0F0F0F0F0FL;
        r = (r | (r << 2)) & 0x3333333333333333L;
        r = (r | (r << 1)) & 0x5555555555555555L;

        r = highBit | r | (r << 1);
        return r;
    }


}
