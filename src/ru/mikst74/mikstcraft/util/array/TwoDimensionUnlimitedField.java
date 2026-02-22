package ru.mikst74.mikstcraft.util.array;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class TwoDimensionUnlimitedField<V> {
    private final Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<V>> fields = new Int2ObjectOpenHashMap<>();

    public V get(int x, int z) {
        Int2ObjectOpenHashMap<V> xline = fields.get(x);
        return xline == null ? null : xline.get(z);
    }

    public void set(int x, int z, V value) {
        Int2ObjectOpenHashMap<V> xline;
        if (!fields.containsKey(x)) {
            xline = new Int2ObjectOpenHashMap<>();
             fields.put(x, xline);
        } else {
            xline = fields.get(x);
        }
        xline.put(z, value);
    }

    public void remove(int x, int z)
    {
        if (fields.containsKey(x)) {
             fields.get(x).remove(z);
             if(fields.get(x).isEmpty())
             {
                 fields.remove(x);
             }
        }
    }

    public boolean containsKey(int x, int z){
        Int2ObjectOpenHashMap<V> xline = fields.get(x);
        return xline != null && xline.containsKey(z);
    }

}
