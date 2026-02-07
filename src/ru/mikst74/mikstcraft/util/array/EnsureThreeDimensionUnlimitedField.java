package ru.mikst74.mikstcraft.util.array;

import ru.mikst74.mikstcraft.model.coo.GridCoo;

import java.util.function.Consumer;
import java.util.function.Function;

public class EnsureThreeDimensionUnlimitedField<G extends GridCoo<G>, V> {
    private final Function<G, V>                  fieldInitializer;
    private final ThreeDimensionUnlimitedField<V> ensureFields = new ThreeDimensionUnlimitedField<>();

    public EnsureThreeDimensionUnlimitedField(Function<G, V> fieldInitializer) {
        this.fieldInitializer = fieldInitializer;
    }

    public boolean isExists(G coo) {
        return ensureFields.containsKey(coo);
    }

    public V get(G coo, Consumer<V> whenCreateNew) {
        V r = ensureFields.get(coo);
        if (r == null) {
            r = fieldInitializer.apply(coo);
            set(coo, r);
            whenCreateNew.accept(r);
        }
        return r;
    }

    public V getIfExists(G coo) {
        return ensureFields.get(coo);
    }

    public void set(G coo, V value) {
        ensureFields.set(coo, value);
    }

    public void remove(G coo) {
        ensureFields.remove(coo);
    }
}
