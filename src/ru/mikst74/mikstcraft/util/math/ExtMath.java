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


    /**
     * Transform 4bit AO factors to 2bit value for greedyMeshing
     * transform matrix
     *     0000   => 3
     *     0001   => 2
     *     0010   => 2
     *     0011   => 1
     *     0100   => 2
     *     0101   => 1
     *     0110   => 2
     *     0111   => 1
     *     1000   => 2
     *     1001   => 0
     *     1010   => 1
     *     1011   => 0
     *     1100   => 1
     *     1101   => 0
     *     1110   => 1
     *     1111   => 0
     * @param x
     * @return
     */
        public static int aoFactor4bitTo2bitTransform(int x) {

            // This constant holds all 16 results (2 bits each)
            // 0x1112666B represents the specific sequence provided in your table
            int magic = 0x1112666B;

            // Step-by-step:
            // 1. (x & 0xF) ensures we only look at the last 4 bits. (no needed in real code)
            // 2. << 1 multiplies by 2 to find the bit offset.
            // 3. >> shifts the target 2-bit result to the front.
            // 4. & 3 extracts those 2 bits.
            return (magic >> ((x /* & 0xF*/) << 1)) & 3;
        }
}
