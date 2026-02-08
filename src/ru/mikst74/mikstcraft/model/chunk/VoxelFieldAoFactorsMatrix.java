package ru.mikst74.mikstcraft.model.chunk;

import static ru.mikst74.mikstcraft.util.math.ExtMath.aoFactor4bitTo2bitTransform;

public class VoxelFieldAoFactorsMatrix {
    /**
     * Полная матрица АО факторов в зависимости от четырех трехбитных солди-масок соседних блоков
     * V1
     * +-----+-----+-----+
     * | a01 |     | a11 |
     * Vp-> +-----+-----+-----+
     * |     | UV  |     |
     * Vm-> +-----+-----+-----+
     * | a00 |     | a10 |
     * U0 +-----+-----+-----+ U1
     * V0
     * ^     ^
     * Um    Up
     * <p>
     * АоМаска стороны - 1 байт
     * 00.00.00.00
     * ^  ^  ^  ^-a00
     * |  |   +---a01
     * |  +-------a10
     * +----------a11
     * <p>
     * Чтобы посчитать a00 - Um & 3 ++ Vm & 3
     * Чтобы посчитать a01 - Um & 6 ++ Vp & 3
     * Чтобы посчитать a10 - Up & 3 ++ Vm & 6
     * Чтобы посчитать a11 - Up & 6 ++ Vp & 6
     * <p>
     * Um = m & 0x7
     * Up = m & 0x38 >> 3
     * Vm = m & 0x1C0 >> 6
     * Vp = m & 0xE00 >> 9
     */
    public static  byte[] AO_FACTOR_MATRIX = new byte[4096];
    private static byte[] CORNER_AO        = new byte[16];
    private static byte AO_L3=3;
    private static byte AO_L2=2;
    private static byte AO_L1=1;
    private static byte AO_L0=0;

    public static void aoFactorMatrixCalculate() {
        CORNER_AO[0b0000] = AO_L3;           //  0000   => 3
        CORNER_AO[0b0001] = AO_L2;           //  0001   => 2
        CORNER_AO[0b0010] = AO_L2;           //  0010   => 2
        CORNER_AO[0b0011] = AO_L1;           //  0011   => 1
        CORNER_AO[0b0100] = AO_L2;           //  0100   => 2
        CORNER_AO[0b0101] = AO_L2;           //  0101   => 1
        CORNER_AO[0b0110] = AO_L2;           //  0110   => 2
        CORNER_AO[0b0111] = AO_L1;           //  0111   => 1
        CORNER_AO[0b1000] = AO_L2;           //  1000   => 2
        CORNER_AO[0b1001] = AO_L2;           //  1001   => 0
        CORNER_AO[0b1010] = AO_L2;           //  1010   => 1
        CORNER_AO[0b1011] = AO_L0;           //  1011   => 0
        CORNER_AO[0b1100] = AO_L1;           //  1100   => 1
        CORNER_AO[0b1101] = AO_L0;           //  1101   => 0
        CORNER_AO[0b1110] = AO_L1;           //  1110   => 1
        CORNER_AO[0b1111] = AO_L0;           //  1111   => 0

        for (int m = 0; m < 4096; m++) {
            int aoUm = m & 0x7;
            int aoUp = (m & 0x38) >> 3;
            int aoVm = (m & 0x1C0) >> 6;
            int aoVp = (m & 0xE00) >> 9;


            int aoUmL = (aoUm & 3) << 2;
            int aoUmH = ((aoUm & 6) >> 1) << 2;
            int aoUpL = (aoUp & 3) << 2;
            int aoUpH = ((aoUp & 6) >> 1) << 2;

            int aoVmL = aoVm & 3;
            int aoVmH = (aoVm & 6) >> 1;
            int aoVpL = aoVp & 3;
            int aoVpH = (aoVp & 6) >> 1;

            int a00_4bit = aoUmL | aoVmL;
            int a01_4bit = aoUpL | aoVmH;
            int a10_4bit = aoUmH | aoVpL;
            int a11_4bit = aoUpH | aoVpH;
            int aoFactor = ((aoFactor4bitTo2bitTransformNew(a00_4bit) << 0) |
                    (aoFactor4bitTo2bitTransformNew(a01_4bit) << 2) |
                    (aoFactor4bitTo2bitTransformNew(a10_4bit) << 4) |
                    (aoFactor4bitTo2bitTransformNew(a11_4bit) << 6)) & 0xFF;
            AO_FACTOR_MATRIX[m] = (byte) aoFactor;
        }

        System.out.println(AO_FACTOR_MATRIX[0b000000000010]);
    }

    private static int aoFactor4bitTo2bitTransformNew(int UVmask) {
        return CORNER_AO[UVmask];
    }
}
