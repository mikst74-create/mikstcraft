package ru.mikst74.mikstcraft.model;

import ru.mikst74.mikstcraft.dictionary.BlockTypeInfo;
import ru.mikst74.mikstcraft.model.coo.WorldCoo;

/**
 * Describes a collision contact.
 */
public class Contact implements Comparable<Contact> {
  private WorldCoo coo;
  private BlockTypeInfo blockTypeInfo;
    /* The collision normal */
    public int nx, ny, nz;
    /* The global position of the collided voxel */
    public int x, y, z;
    /* The collision time */
    public   float t;

    public Contact(float t, int x, int y, int z) {
        this.t = t;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int compareTo(Contact o) {
        /* Resolve first by Y contacts, then by distance */
        return ny != o.ny ? o.ny - ny : Float.compare(t, o.t);
    }

    public Contact(WorldCoo coo, BlockTypeInfo blockTypeInfo) {
        this.coo           = coo;
        this.blockTypeInfo = blockTypeInfo;
    }

    public String toString() {
        return "{" + x + "|" + y + "|" + z + " " + nx + " " + ny + " " + nz + " @ " + t + "}";
    }
}
