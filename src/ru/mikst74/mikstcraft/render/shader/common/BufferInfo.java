package ru.mikst74.mikstcraft.render.shader.common;

import lombok.Getter;

@Getter
public class BufferInfo {
    public static int BI_STATIC = 0;
    public static int BI_DYNAMIC = 1;

    private int id;
    private int bo;
    private int size;
    private int flags;

    public BufferInfo(int id, int bo, int size, int flags) {
        this.id = id;
        this.bo = bo;
        this.size = size;
        this.flags = flags;
    }

}
