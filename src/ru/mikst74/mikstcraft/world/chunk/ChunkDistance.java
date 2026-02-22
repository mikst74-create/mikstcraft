package ru.mikst74.mikstcraft.world.chunk;

import org.joml.Vector3f;
import ru.mikst74.mikstcraft.model.Person;
import ru.mikst74.mikstcraft.model.coo.ChunkCoo;
import ru.mikst74.mikstcraft.model.coo.WorldCoo;
import ru.mikst74.mikstcraft.settings.GameProperties;

import static ru.mikst74.mikstcraft.model.coo.WCVConverter.staticWCV;

/**
 * Created by Mikhail Krinitsyn on 10.01.2026
 */
public class ChunkDistance {
    public static Person player;

    /**
     * Compute the distance from the player's position to the center of the chunk at
     * <code>(cx, cz)</code>.
     *
     * @param cCoo
     */
    public static double distToChunk(ChunkCoo cCoo) {
        return distToChunk(player.getPosition(), cCoo);
    }

    public static double distToChunk(Vector3f pos, ChunkCoo cCoo) {
        staticWCV.assign(cCoo); //TODO потенциальная проблема при мультитрединге! уходить от статика
        WorldCoo wCoo = staticWCV.getW();
        double dx = pos.x - wCoo.getX();
        double dy = pos.y - wCoo.getY();
        double dz = pos.z - wCoo.getZ();

        return dx * dx + dy * dy + dz * dz;
    }


    /**
     * Determine whether the chunk at <code>(x, z)</code> is within render distance.
     *
     * @param coo
     */
    public static boolean chunkInRenderDistance(ChunkCoo coo) {
        boolean b = distToChunk(coo) < GameProperties.MAX_RENDER_DISTANCE_METERS2;
//        if(!b){
//            "Chunk "+coo
//        }
        return b;
    }

    public static boolean chunkInRenderDistance(Vector3f pos, ChunkCoo coo) {
        if (coo.getY() == 0 && coo.getX() == 0 && coo.getZ() == 0) {
            return distToChunk(pos, coo) < GameProperties.MAX_RENDER_DISTANCE_METERS2;
        }
        return false;
    }

}
