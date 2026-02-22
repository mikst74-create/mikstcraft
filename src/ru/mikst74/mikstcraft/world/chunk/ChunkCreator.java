package ru.mikst74.mikstcraft.world.chunk;

import lombok.Getter;
import ru.mikst74.mikstcraft.model.chunk.Chunk;
import ru.mikst74.mikstcraft.model.chunk.VoxelField;
import ru.mikst74.mikstcraft.model.coo.ChunkCoo;
import ru.mikst74.mikstcraft.storage.WorldSaver;
import ru.mikst74.mikstcraft.util.time.Profiler;
import ru.mikst74.mikstcraft.world.generator.WorldMapGenerator;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Mikhail Krinitsyn on 10.01.2026
 */
@Getter
public class ChunkCreator {
    private final WorldMapGenerator mapGenerator;

    public ChunkCreator(WorldMapGenerator mapGenerator) {
        this.mapGenerator = mapGenerator;
    }

    /**
     * Create a chunk at the position <code>(cx, cz)</code> (in units of whole chunks).
     *
     * @param coo the coordinate of the chunk position
     */

    public Chunk createChunk(ChunkCoo coo) {
        Chunk chunk = new Chunk(coo);

        if (!WorldSaver.loadChunk(chunk)) {
            AtomicReference<VoxelField> field = new AtomicReference<>();
            Profiler.profile("MapGenerator().createVoxelField", () -> {
                field.set(mapGenerator.createVoxelField(coo));
            });
            Profiler.profile("chunk.setVoxelFieldData", () -> chunk.setVoxelFieldData(field.get()));
        }

        return chunk;
    }


}
