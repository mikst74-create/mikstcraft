package ru.mikst74.mikstcraft.util.math;

import lombok.Getter;
import org.joml.Vector3f;

@Getter
public class Hitbox {
    public final Vector3f pos;
    public final Vector3f offset;
    public final Vector3f size;

    public final Vector3f actual;

    public boolean isEmpty;

    public Hitbox() {
        this.pos     = new Vector3f();
        this.offset  = new Vector3f();
        this.size    = new Vector3f();
        this.actual  = new Vector3f();
        this.isEmpty = false;
    }

    public Hitbox(Vector3f pos, Vector3f size) {
        this.pos     = pos;
        this.size    = size;
        this.offset  = new Vector3f();
        this.actual  = new Vector3f();
        this.isEmpty = false;
        this.offset.set(0);
        this.actual.set(pos);
    }

    public Hitbox(float posX, float posY, float posZ, float sizeX, float sizeY, float sizeZ) {
        this.pos     = new Vector3f(posX, posY, posZ);
        this.size    = new Vector3f(sizeX, sizeY, sizeZ);
        this.offset  = new Vector3f();
        this.actual  = new Vector3f();
        this.isEmpty = false;
        this.offset.set(0);
        this.actual.set(pos);
    }

    public Hitbox assign(float posX, float posY, float posZ, float sizeX, float sizeY, float sizeZ) {
        this.pos.x  = posX;
        this.pos.y  = posY;
        this.pos.z  = posZ;
        this.size.x = sizeX;
        this.size.y = sizeY;
        this.size.z = sizeZ;
        this.offset.set(0);
        this.actual.set(pos);
        return this;
    }

    public Hitbox assign(Hitbox hitbox) {
        this.pos.set(hitbox.pos);
        this.size.set(hitbox.size);
        this.offset.set(0);
        this.actual.set(pos);
        return this;
    }

    public Hitbox offset(Vector3f delta) {
        this.offset.add(delta);
        this.actual.set(pos).add(delta);
        return this;
    }

    public Hitbox setOffset(Vector3f offset) {
        this.offset.set(offset);
        this.actual.set(pos).add(offset);
        return this;
    }

    public Hitbox offsetX(float dx) {
        this.offset.x += dx;
        this.actual.x = pos.x + offset.x;
        return this;
    }

    public Hitbox offsetY(float dy) {
        this.offset.y += dy;
        this.actual.y = pos.y + offset.y;
        return this;
    }

    public Hitbox offsetZ(float dz) {
        this.offset.z += dz;
        this.actual.z = pos.z + offset.z;
        return this;
    }

    public void setEmpty() {
        isEmpty = true;
        pos.x   = Float.POSITIVE_INFINITY;
        pos.y   = Float.POSITIVE_INFINITY;
        pos.z   = Float.POSITIVE_INFINITY;
        size.x  = 0;
        size.y  = 0;
        size.z  = 0;
        this.offset.set(0);
        this.actual.set(pos);
    }
}
