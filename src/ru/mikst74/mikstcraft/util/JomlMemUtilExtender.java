package ru.mikst74.mikstcraft.util;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static ru.mikst74.mikstcraft.util.ByteSizeGetter.sizeOf;

public class JomlMemUtilExtender {


    public static long getToAddress(Object o, long address) {
        if (o instanceof Vector4f) {
            return getToAddress((Vector4f) o, address);
        }
        if (o instanceof Vector3f) {
            return getToAddress((Vector3f) o, address);
        }
        if (o instanceof Matrix4f) {
            return getToAddress((Matrix4f) o, address);
        }
        throw new RuntimeException("Can't getToAddress for class " + o.getClass().getCanonicalName());
    }

    public static long getToAddress(Matrix4f v, long address) {
        v.getToAddress(address);
        return address + sizeOf(v);
    }

    public static long getToAddress(Vector4f v, long address) {
        v.getToAddress(address);
        return address + sizeOf(v);
    }
    public static long getToAddress(Vector3f v, long address) {
        v.getToAddress(address);
        return address + sizeOf(v);
    }


}
