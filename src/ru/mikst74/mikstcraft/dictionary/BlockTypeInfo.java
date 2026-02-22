package ru.mikst74.mikstcraft.dictionary;

import lombok.Getter;
import ru.mikst74.mikstcraft.model.FaceMaterial;
import ru.mikst74.mikstcraft.model.NeighborCode;
import ru.mikst74.mikstcraft.util.math.Hitbox;

import java.util.Objects;

@Getter
public class BlockTypeInfo {
    private final       short  id;
    private final       String name;
    public static final int    FULL_SOLID_BLOCK    = 63;
    public static final int    FULL_NO_SOLID_BLOCK = 0;
    /**
     * see NeighborCode for details
     * 6 bit mask
     */
    private final       int    solidMask;
    private final       Hitbox hitbox;

    private final FaceMaterial[] faceMaterial;

    public BlockTypeInfo(int id, String name, int solidMask, FaceMaterial faceMaterial) {
        this.id           = (short) id;
        this.name         = name;
        this.solidMask    = solidMask;
        this.faceMaterial = new FaceMaterial[]{faceMaterial, faceMaterial, faceMaterial, faceMaterial, faceMaterial, faceMaterial};
        this.hitbox       = new Hitbox(0, 0, 0, 1, 1, 1);
        if (solidMask != FULL_SOLID_BLOCK) {
            hitbox.setEmpty();
        }

    }

    public boolean isSolidFace(NeighborCode nc) {
        return (solidMask & nc.getB()) > 0;
    }

    public int ifSolidFace(NeighborCode nc, int r) {
        return (solidMask & nc.getB()) > 0 ? r : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BlockTypeInfo that = (BlockTypeInfo) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "BlockTypeInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
