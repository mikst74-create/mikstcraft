package ru.mikst74.mikstcraft.model.coo;

import lombok.Getter;
import lombok.Setter;
import ru.mikst74.mikstcraft.model.NeighborCode;

@Getter
@Setter
public abstract class GridCoo<T extends GridCoo<T>> {
    protected int x, y, z;

    public GridCoo() {
        assign(0, 0, 0);
    }


    public GridCoo(GridCoo<T> coo) {
        this(coo.x, coo.y, coo.z);
    }

    public void assign(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public GridCoo(int x, int y, int z) {
        this();
        assign(x, y, z);
    }


    public T step(NeighborCode nc) {
        return addX(nc.getDx()).
                addY(nc.getDy()).
                addZ(nc.getDz());
    }


    public T add(GridCoo<T> s) {
        addX(s.getX());
        addY(s.getY());
        addZ(s.getZ());
        return (T) this;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GridCoo gridCoo = (GridCoo) o;
        return x == gridCoo.x && y == gridCoo.y && z == gridCoo.z;
    }

    @Override
    public int hashCode() {
        return x | (y << 10) | (z << 20);
    }


    public T assign(GridCoo<T> v) {
        x = v.x;
        y = v.y;
        z = v.z;
        return (T) this;
    }

    public T incX() {
        x = x + 1;
        return (T) this;
    }

    public T addXYZ(int dx, int dy, int dz) {
        x = x + dx;
        y = y + dy;
        z = z + dz;
        return (T) this;
    }

    public T addX(int d) {
        x = x + d;
        return (T) this;
    }

    public T decX() {
        x = x - 1;
        return (T) this;
    }

    public T incY() {
        y = y + 1;
        return (T) this;
    }

    public T decY() {
        y = y - 1;
        return (T) this;
    }

    public T addY(int d) {
        y = y + d;
        return (T) this;
    }

    public T incZ() {
        z = z + 1;
        return (T) this;
    }

    public T decZ() {
        z = z - 1;
        return (T) this;
    }

    public T addZ(int d) {
        z = z + d;
        return (T) this;
    }

    public int get(int axis) {
        if (axis == CooConstant.X_AXIS) {
            return getX();
        }
        if (axis == CooConstant.Y_AXIS) {
            return getY();
        }
        if (axis == CooConstant.Z_AXIS) {
            return getZ();
        }
        throw new RuntimeException("unknown axis " + axis);
    }

    public T set(int axis, int d) {
        if (axis == CooConstant.X_AXIS) {
            setX(d);
            return (T) this;
        }
        if (axis == CooConstant.Y_AXIS) {
            setY(d);
            return (T) this;
        }
        if (axis == CooConstant.Z_AXIS) {
            setZ(d);
            return (T) this;
        }
        throw new RuntimeException("unknown axis " + axis);
    }

    public T add(int axis, int d) {
        if (axis == CooConstant.X_AXIS) {
            return addX(d);
        }
        if (axis == CooConstant.Y_AXIS) {
            return addY(d);
        }
        if (axis == CooConstant.Z_AXIS) {
            return addZ(d);
        }
        throw new RuntimeException("unknown axis " + axis);
    }

    public T inc(int axis) {
        if (axis == CooConstant.X_AXIS) {
            return incX();
        }
        if (axis == CooConstant.Y_AXIS) {
            return incY();
        }
        if (axis == CooConstant.Z_AXIS) {
            return incZ();
        }
        throw new RuntimeException("unknown axis " + axis);
    }

    public T dec(int axis) {
        if (axis == CooConstant.X_AXIS) {
            return decX();
        }
        if (axis == CooConstant.Y_AXIS) {
            return decY();
        }
        if (axis == CooConstant.Z_AXIS) {
            return decZ();
        }
        throw new RuntimeException("unknown axis " + axis);
    }
}
