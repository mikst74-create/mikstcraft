package ru.mikst74.mikstcraft.render.chunk;

import lombok.Getter;
import org.joml.Vector3f;
import ru.mikst74.mikstcraft.model.NeighborCode;
import ru.mikst74.mikstcraft.model.chunk.Chunk;
import ru.mikst74.mikstcraft.model.coo.WorldCoo;
import ru.mikst74.mikstcraft.util.array.FirstFitFreeListAllocator2;
import ru.mikst74.mikstcraft.util.floodfill.ChunkVisibility;

import java.util.ArrayList;
import java.util.List;

import static ru.mikst74.mikstcraft.model.NeighborCode.*;

@Getter
public class RenderedChunk {
    private final Chunk                               chunk;
    /**
     * The region associated with this chunk.
     */
    public        FirstFitFreeListAllocator2.Region[] r;
    /**
     * The index in per-chunk buffers.
     */
    public        int                                 index;
    /**
     * Whether this chunk can be rendered.
     */
    public        boolean                             ready;
    public        long                                lastMeshUpdateTime = 0;
    public        boolean                             isVisible;
    public        boolean                             inFrustrum;
    private       ChunkVisibility.VisibilityResult    facesVisibility;

    public RenderedChunk(Chunk chunk) {
        this.chunk           = chunk;
        this.r               = new FirstFitFreeListAllocator2.Region[6];
        this.facesVisibility = chunk.getVoxelField().getAirFlowThroughChunk();
    }

    //proxy method
    public WorldCoo getWCoo() {
        return chunk.getWCoo();
    }

    @Override
    public String toString() {
        return "RenderedChunk{" +
                "chunk=" + chunk +
                '}';
    }

    public List<NeighborCode> getRenderedFaces(Vector3f cameraPosition) {
        List<NeighborCode> res = new ArrayList<>(5); //max 5 rendered faces possible
        WorldCoo wCoo = chunk.getWCoo();
        if (cameraPosition.x < wCoo.getX()) {
            res.add(XM);
        } else if (cameraPosition.x > wCoo.getX() + 16) {
            res.add(XP);
        } else {
            res.add(XP);
            res.add(XM);
        }

        if (cameraPosition.y < wCoo.getY()) {
            res.add(YM);
        } else if (cameraPosition.y > wCoo.getY() + 16) {
            res.add(YP);
        } else {
            res.add(YP);
            res.add(YM);
        }

        if (cameraPosition.z < wCoo.getZ()) {
            res.add(ZM);
        } else if (cameraPosition.z > wCoo.getZ() + 16) {
            res.add(ZP);
        } else {
            res.add(ZP);
            res.add(ZM);
        }

        return res;
    }

}
