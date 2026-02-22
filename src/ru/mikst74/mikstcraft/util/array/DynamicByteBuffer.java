package ru.mikst74.mikstcraft.util.array;

import ru.mikst74.mikstcraft.settings.GameProperties;

import static ru.mikst74.mikstcraft.settings.GameProperties.DEBUG;
import static org.lwjgl.system.MemoryUtil.memPutInt;
import static org.lwjgl.system.MemoryUtil.memPutShort;
import static org.lwjgl.system.MemoryUtil.nmemAlloc;
import static org.lwjgl.system.MemoryUtil.nmemFree;
import static org.lwjgl.system.MemoryUtil.nmemRealloc;

/**
 * Dynamically growable ByteBuffer.
 */
public class DynamicByteBuffer {
    public long addr;
    public int pos, cap;

    /**
     * Allocate a ByteBuffer with the given initial capacity.
     */
    public DynamicByteBuffer(int initialCapacity) {
        addr = nmemAlloc(initialCapacity);
        cap = initialCapacity;
        if (DEBUG) {
            System.out.println("Creating new DynamicByteBuffer with capacity [" + GameProperties.INT_FORMATTER.format(cap / 1024) + " KB]");
        }
    }

    private void grow() {
        int newCap = (int) (cap * 1.75f);
        if (DEBUG) {
            System.out.println(
                    "Growing DynamicByteBuffer from [" + GameProperties.INT_FORMATTER.format(cap / 1024) + " KB] to [" + GameProperties.INT_FORMATTER.format(newCap / 1024) + " KB]");
        }
        long newAddr = nmemRealloc(addr, newCap);
        cap = newCap;
        addr = newAddr;
    }

    public void free() {
        if (DEBUG) {
            System.out.println("Freeing DynamicByteBuffer (used " + GameProperties.PERCENT_FORMATTER.format((float) pos / cap) + " of capacity)");
        }
        nmemFree(addr);
    }

    public DynamicByteBuffer putInt(int v) {
        if (cap - pos < Integer.BYTES) {
            grow();
        }
        return putIntNoGrow(v);
    }

    private DynamicByteBuffer putIntNoGrow(int v) {
        memPutInt(addr + pos, v);
        pos += Integer.BYTES;
        return this;
    }

    public DynamicByteBuffer putShort(int v) {
        if (cap - pos < Short.BYTES) {
            grow();
        }
        return putShortNoGrow(v);
    }

    private DynamicByteBuffer putShortNoGrow(int v) {
        memPutShort(addr + pos, (short) v);
        pos += Short.BYTES;
        return this;
    }
}
