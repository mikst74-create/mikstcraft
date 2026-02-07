package ru.mikst74.mikstcraft.util.array;

import java.util.function.BiFunction;

public class EnsureTwoDimensionUnlimitedField<V> {
    private final BiFunction<Integer, Integer, V> fieldInitializer;
    private final TwoDimensionUnlimitedField<V>   ensureFields = new TwoDimensionUnlimitedField<>();

    public EnsureTwoDimensionUnlimitedField(BiFunction<Integer, Integer, V> fieldInitializer) {
        this.fieldInitializer = fieldInitializer;
    }

    public boolean isExists(int x, int z) {
        return ensureFields.containsKey(x, z);
    }

    public V get(int x, int z) {
        V r = ensureFields.get(x, z);
        if (r == null) {
            r = fieldInitializer.apply(x, z);
            set(x, z, r);
        }
        return r;
    }

    public V getIfExists(int x, int z) {
        return ensureFields.get(x, z);
    }

    public void set(int x, int z, V value) {
        ensureFields.set(x, z, value);
    }

    public void remove(int x, int z) {
        ensureFields.remove(x, z);
    }
}
