package ru.mikst74.mikstcraft.util;

public class DebugHelper {
    public static String format64BitLongAs64String(long value) {
        return String.format("%64s", Long.toBinaryString(value)).replace(' ', '0').replaceAll("..", "$0 ");
    }

    public static String format32BitLongAs64String(int value) {
        return String.format("%32s", Integer.toBinaryString(value)).replace(' ', '0').replaceAll(".", ".$0 ");
    }
}
