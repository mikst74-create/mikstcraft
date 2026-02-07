package ru.mikst74.mikstcraft.util;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ByteSizeGetter {
    public static int sizeOf(Matrix4f v) {
        return Float.BYTES * 16;
    }

    public static int sizeOf(Vector4f v) {
        return Float.BYTES * 4;
    }

    public static int sizeOf(Vector3f v) {
        return Float.BYTES * 3;
    }
}
