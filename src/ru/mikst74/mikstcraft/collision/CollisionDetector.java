package ru.mikst74.mikstcraft.collision;

import org.joml.Vector3f;
import ru.mikst74.mikstcraft.dictionary.BlockTypeInfo;
import ru.mikst74.mikstcraft.model.Contact;
import ru.mikst74.mikstcraft.model.coo.WorldCoo;
import ru.mikst74.mikstcraft.util.math.Hitbox;
import ru.mikst74.mikstcraft.world.WorldMap;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.floor;

public class CollisionDetector {
    private       WorldCoo coo       = new WorldCoo();
    private final WorldMap worldMap;
    private final Hitbox   newHitbox = new Hitbox();

    public CollisionDetector(WorldMap worldMap) {
        this.worldMap = worldMap;
    }

    /**
     * Handle any collisions with the player and the voxels.
     */
    public void handleCollisions(Hitbox hitbox, Vector3f delta) {
//    public void handleCollisions(float dt, Vector3f v, Vector3f p) {
        List<Contact> contacts = new ArrayList<>();
        collisionDetection(hitbox, delta, contacts);
//        collisionResponse(dt, v, p, contacts);
    }

    /**
     * Detect possible collision candidates.
     */
    private void collisionDetection(Hitbox hitbox, Vector3f delta, List<Contact> contacts) {
        newHitbox.assign(hitbox);
        // 1. Check movement on Y-axis (Gravity/Jumping)
        if (isCollidingY(hitbox, delta.y, contacts)) {
            delta.y = 0;
        }
//         2. Check movement on X-axis
        if (isCollidingX(hitbox, delta.x, contacts)) {
            delta.x = 0;
        }

        // 3. Check movement on Z-axis
        if (isCollidingZ(hitbox, delta.z, contacts)) {
            delta.z = 0;
        }

    }

    private boolean isCollidingX(Hitbox hitbox, float dx, List<Contact> contacts) {
        int x = (int) (hitbox.actual.x + (dx > 0 ? hitbox.size.x + dx-1: dx));

        for (int y = (int) floor(hitbox.actual.y); y <= (int) floor(hitbox.actual.y + hitbox.size.y); y++) {
            for (int z = (int) floor(hitbox.actual.z); z <= (int) floor(hitbox.actual.z + hitbox.size.z); z++) {
                if (!checkCollide(contacts, x, y, z)) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }


    private boolean isCollidingY(Hitbox hitbox, float dy, List<Contact> contacts) {
        int y = (int) (hitbox.actual.y + (dy > 0 ? hitbox.size.y + dy : dy));

        for (int x = (int) floor(hitbox.actual.x); x <= (int) floor(hitbox.actual.x + hitbox.size.x); x++) {
            for (int z = (int) floor(hitbox.actual.z); z <= (int) floor(hitbox.actual.z + hitbox.size.z); z++) {
                if (!checkCollide(contacts, x, y, z)) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    private boolean isCollidingZ(Hitbox hitbox, float dz, List<Contact> contacts) {
        int z = (int) (hitbox.actual.z + (dz > 0 ? hitbox.size.z + dz-1 : dz));

        for (int x = (int) floor(hitbox.actual.x); x <= (int) floor(hitbox.actual.x + hitbox.size.x); x++) {
            for (int y = (int) floor(hitbox.actual.y); y <= (int) floor(hitbox.actual.y + hitbox.size.y); y++) {
                if (!checkCollide(contacts, x, y, z)) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    private boolean checkCollide(List<Contact> contacts, int x, int y, int z) {
        coo.assign(x, y, z);
        BlockTypeInfo blockInfo = worldMap.getVoxel(coo);
        if (blockInfo.getHitbox().isEmpty()) {
            return false;
        }
        contacts.add(new Contact(coo, blockInfo));
        return true;
    }

    private boolean isColliding(Hitbox add) {
        return false;
    }

    /**
     * Compute the exact collision point between the player and the voxel at <code>(x, y, z)</code>.
     */
//    private void intersectSweptAabbAabb(int x, int y, int z, Hitbox hitbox, Vector3f
//            delta, List<Contact> contacts) {
//        /*
//         * https://www.gamedev.net/tutorials/programming/general-and-gameplay-programming/swept-aabb-collision-detection-and-response-r3084/
//         */
//        float pxmax = px + GameProperties.PLAYER_WIDTH,
//                pxmin = px - GameProperties.PLAYER_WIDTH,
//                pymax = py + GameProperties.PLAYER_HEIGHT - GameProperties.PLAYER_EYE_HEIGHT,
//                pymin = py - GameProperties.PLAYER_EYE_HEIGHT,
//                pzmax = pz + GameProperties.PLAYER_WIDTH,
//                pzmin = pz - GameProperties.PLAYER_WIDTH;
//        float xInvEntry = dx > 0f ? -pxmax : 1 - pxmin, xInvExit = dx > 0f ? 1 - pxmin : -pxmax;
//        boolean xNotValid = dx == 0 || load(x + (dx > 0 ? -1 : 1), y, z) != AIR_BLOCK;
//        float xEntry = xNotValid ? NEGATIVE_INFINITY : xInvEntry / dx,
//                xExit = xNotValid ? POSITIVE_INFINITY : xInvExit / dx;
//
//        float yInvEntry = dy > 0f ? -pymax : 1 - pymin, yInvExit = dy > 0f ? 1 - pymin : -pymax;
//        boolean yNotValid = dy == 0 || load(x, y + (dy > 0 ? -1 : 1), z) != AIR_BLOCK;
//        float yEntry = yNotValid ? NEGATIVE_INFINITY : yInvEntry / dy,
//                yExit = yNotValid ? POSITIVE_INFINITY : yInvExit / dy;
//
//        float zInvEntry = dz > 0f ? -pzmax : 1 - pzmin, zInvExit = dz > 0f ? 1 - pzmin : -pzmax;
//        boolean zNotValid = dz == 0 || load(x, y, z + (dz > 0 ? -1 : 1)) != AIR_BLOCK;
//        float zEntry = zNotValid ? NEGATIVE_INFINITY : zInvEntry / dz,
//                zExit = zNotValid ? POSITIVE_INFINITY : zInvExit / dz;
//
//        float tEntry = max(max(xEntry, yEntry), zEntry), tExit = min(min(xExit, yExit), zExit);
//        if (tEntry < -.5f || tEntry > tExit) {
//            return;
//        }
//        Contact c;
//        contacts.add(c = new Contact(tEntry, x, y, z));
//        if (xEntry == tEntry) {
//            c.nx = dx > 0 ? -1 : 1;
//        } else if (yEntry == tEntry) {
//            c.ny = dy > 0 ? -1 : 1;
//        } else {
//            c.nz = dz > 0 ? -1 : 1;
//        }
//    }
//
//    /**
//     * Respond to all found collision contacts.
//     */
//    private void collisionResponse(float dt, Vector3f v, Vector3f p, List<Contact> contacts) {
//        sort(contacts);
//        int
//                minX = Integer.MIN_VALUE,
//                maxX = Integer.MAX_VALUE,
//                minY = Integer.MIN_VALUE,
//                maxY = Integer.MAX_VALUE,
//                minZ = Integer.MIN_VALUE,
//                maxZ = Integer.MAX_VALUE;
//        float elapsedTime = 0f;
//        float dx = v.x * dt, dy = v.y * dt, dz = v.z * dt;
//        for (int i = 0; i < contacts.size(); i++) {
//            Contact contact = contacts.get(i);
//            if (contact.x <= minX || contact.y <= minY || contact.z <= minZ || contact.x >= maxX || contact.y >= maxY || contact.z >= maxZ) {
//                continue;
//            }
//            float t = contact.t - elapsedTime;
//            p.add(dx * t, dy * t, dz * t);
//            elapsedTime += t;
//            if (contact.nx != 0) {
//                minX = dx < 0 ? max(minX, contact.x) : minX;
//                maxX = dx < 0 ? maxX : min(maxX, contact.x);
//                v.x  = 0f;
//                dx   = 0f;
//            } else if (contact.ny != 0) {
//                minY = dy < 0 ? max(minY, contact.y) : contact.y - (int) GameProperties.PLAYER_HEIGHT;
//                maxY = dy < 0 ? contact.y + (int) ceil(GameProperties.PLAYER_HEIGHT) + 1 : min(maxY, contact.y);
//                v.y  = 0f;
//                dy   = 0f;
//            } else if (contact.nz != 0) {
//                minZ = dz < 0 ? max(minZ, contact.z) : minZ;
//                maxZ = dz < 0 ? maxZ : min(maxZ, contact.z);
//                v.z  = 0f;
//                dz   = 0f;
//            }
//        }
//        float trem = 1f - elapsedTime;
//        p.add(dx * trem, dy * trem, dz * trem);
//    }
}
