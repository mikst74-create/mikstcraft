package ru.mikst74.mikstcraft.util.array;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import ru.mikst74.mikstcraft.model.coo.GridCoo;

public class ThreeDimensionUnlimitedField<V> {
    private final Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<V>>> fields = new Int2ObjectOpenHashMap<>();

    public V get(GridCoo coo) {
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<V>> xline = fields.get(coo.getX());
        if (xline == null) {
            return null;
        }

        Int2ObjectOpenHashMap<V> yline = xline.get(coo.getY());
        if (yline == null) {
            return null;
        }

        return yline.get(coo.getZ());
    }

    public void set(GridCoo coo, V value) {
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<V>> xline;
        Int2ObjectOpenHashMap<V> yline;

        // Check, get or create x line
        if (!fields.containsKey(coo.getX())) {
            xline = new Int2ObjectOpenHashMap<>();
            fields.put(coo.getX(), xline);
        } else {
            xline = fields.get(coo.getX());
        }

        // Check, get or create y line
        if (!xline.containsKey(coo.getY())) {
            yline = new Int2ObjectOpenHashMap<>();
            xline.put(coo.getY(), yline);
        } else {
            yline = xline.get(coo.getY());
        }

        // put the value
        yline.put(coo.getZ(), value);
    }

    public void remove(GridCoo coo) {
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<V>> xline;
        Int2ObjectOpenHashMap<V> yline;
        if (fields.containsKey(coo.getX())) {
            xline = fields.get(coo.getX());
            if (xline.containsKey(coo.getY())) {
                yline = xline.get(coo.getY());
                yline.remove(coo.getZ());
                if (yline.isEmpty()) {
                    xline.remove(coo.getY());
                }
            }
            if (fields.get(coo.getX()).isEmpty()) {
                fields.remove(coo.getX());
            }
        }
    }

    public boolean containsKey(GridCoo coo) {
        Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<V>> xline = fields.get(coo.getX());
        if (xline == null) return false;

        Int2ObjectOpenHashMap<V> yline = xline.get(coo.getY());
        return yline != null && yline.containsKey(coo.getZ());
    }

}
