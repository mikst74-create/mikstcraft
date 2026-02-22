package ru.mikst74.mikstcraft.render.chunk;

import lombok.AllArgsConstructor;
import ru.mikst74.mikstcraft.model.NeighborCode;

import java.util.List;

@AllArgsConstructor
public class ChunkTraversal {
    public RenderedChunk chunk;
    public NeighborCode  entryFace;
    public List<NeighborCode> possibleExitFace;


}
