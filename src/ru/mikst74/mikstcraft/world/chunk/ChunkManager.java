package ru.mikst74.mikstcraft.world.chunk;

import lombok.Getter;
import ru.mikst74.mikstcraft.dictionary.BlockTypeInfo;
import ru.mikst74.mikstcraft.model.chunk.Chunk;
import ru.mikst74.mikstcraft.model.coo.ChunkCoo;
import ru.mikst74.mikstcraft.model.coo.WCVConverter;
import ru.mikst74.mikstcraft.model.coo.WorldCoo;
import ru.mikst74.mikstcraft.util.array.EnsureThreeDimensionUnlimitedField;
import ru.mikst74.mikstcraft.world.generator.WorldMapGenerator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Mikhail Krinitsyn on 10.01.2026
 */
@Getter
public class ChunkManager {
    private static final int MAX_CACHED_CHUNKS = 10000;
    private final WorldMapGenerator mapGenerator;
    private final ChunkNeighborSupplier chunkNeighborSupplier;
    private final ChunkCreator          chunkCreator;

    /**
     * All active chunks by their coordinates.
     */

    private final EnsureThreeDimensionUnlimitedField<ChunkCoo, Chunk> ensuredChunkMap;

    /**
     * LRU cache for voxel fields of recently used chunks.
     * <p>
     * This is used for querying for hit detection and for modifying voxels.
     */
    private final Map<ChunkCoo, Chunk> chunkCache = new LinkedHashMap<ChunkCoo, Chunk>(MAX_CACHED_CHUNKS + 1, 0.75f, true) {
        private static final long serialVersionUID = 1L;

        public boolean removeEldestEntry(Map.Entry<ChunkCoo, Chunk> eldest) {
            boolean toRemove = size() > MAX_CACHED_CHUNKS;
            if (toRemove) {
                beforeRemoveFromCache();
            }
            return toRemove;
        }
    };

    public ChunkManager(WorldMapGenerator mapGenerator) {
        ChunkFrontierSupplier.initStatic(this);
        this.mapGenerator          = mapGenerator;
        this.chunkCreator          = new ChunkCreator(mapGenerator);
        this.chunkNeighborSupplier = new ChunkNeighborSupplier(this);
        this.ensuredChunkMap       = new EnsureThreeDimensionUnlimitedField<>(chunkCreator::createChunk);
    }

    /**
     * before removing chunk from cache need save it to disk
     */
    private void beforeRemoveFromCache() {
        // TODO
    }


    public void remove(Chunk chunk) {
        chunkCache.remove(chunk.getCoo());
    }

    public int count() {
        return chunkCache.size();
    }


    public Chunk getChunk(ChunkCoo coo) {
        return ensuredChunkMap.get(coo, this::addChunk);
    }

    public Chunk getChunkIfExists(ChunkCoo coo) {
        return ensuredChunkMap.getIfExists(coo);
    }

    public void addChunk( Chunk chunk) {
        ensuredChunkMap.set(chunk.getCoo(), chunk);
        chunkCache.put(chunk.getCoo(), chunk);
        chunkNeighborSupplier.copyVoxelFieldFromNeighborAfterChunkCreated(chunk);
    }

    /**
     * Compute the used memory in all buffer objects for all chunks.
     */
    public int computePerFaceBufferObjectSize() {
        int bytes = 0;
//        for (Chunk c : allChunks) {
//            if (c.r != null) {
//                bytes += c.r.len * (verticesPerFace * voxelVertexSize + indicesPerFace * Short.BYTES);
//            }
//        }
        return bytes;
    }


    public List<Chunk> getAllNotSavedChunk() {
        return chunkCache.entrySet().stream()
                .map(Map.Entry::getValue)
                .filter(Chunk::isNotSaved)
                .collect(Collectors.toList());

    }

    public void setVoxel(WorldCoo wCoo, BlockTypeInfo v) {
        WCVConverter wcv = new WCVConverter(wCoo);
        Chunk chunk = getChunk(wcv.getC());
        if (chunk == null) {
            System.err.println("Tried to store(" + wCoo + ", " + v + ") to non-existing chunk");
            return;
        }
        chunk.setVoxel(wcv.getV(), v);
        //TODO добавить проверки, какие именно воксели изменились и копировать только если на границе и в нужных соседей
        chunkNeighborSupplier.copyVoxelFieldFromNeighborAfterChunkCreated(chunk);
    }
}
