package ru.mikst74.mikstcraft.world;

import lombok.Getter;
import ru.mikst74.mikstcraft.dictionary.BlockTypeInfo;
import ru.mikst74.mikstcraft.model.chunk.Chunk;
import ru.mikst74.mikstcraft.model.coo.ChunkCoo;
import ru.mikst74.mikstcraft.model.coo.WCVConverter;
import ru.mikst74.mikstcraft.model.coo.WorldCoo;
import ru.mikst74.mikstcraft.world.chunk.ChunkManager;
import ru.mikst74.mikstcraft.world.generator.WorldMapGenerator;

import static ru.mikst74.mikstcraft.dictionary.BlockTypeDictionary.AIR_BLOCK;

/**
 * Created by Mikhail Krinitsyn on 09.01.2026
 */
@Getter
public class WorldMap {
    private static final int          ENSURED_REGION_SIZE = 1; // 5 chunk to each side from player position
    private              ChunkManager chunkManager;
    //=
//               new ChunkManager(new NoiseTerrainGenerator());
//      new ChunkManager(new Step3TerrainGenerator());
//      new ChunkManager(new FlatTerrainGenerator());
    //   new ChunkManager(new FlatTerrain3DGenerator());
//      new ChunkManager(new OneVoxelPerChunkCube1x1Terrain3DGenerator());
//            new ChunkManager(new EightVoxelPerChunkCube2x2Terrain3DGenerator());
//      new ChunkManager(new FreeCodeTerrain3DGenerator());
//      new ChunkManager(new NearEgdesTerrain3DGenerator());
//      new ChunkManager(new OnlyEgdesTerrain3DGenerator());
//      new ChunkManager(new NoiseTerrain3DGenerator());
//      new ChunkManager(new StairsTerrain3DGenerator());
//            new ChunkManager(new FullEgdesTerrain3DGenerator());


//    public Person player;

    public WorldMap(WorldMapGenerator generator) {
        chunkManager = new ChunkManager(generator);
    }

    /**
     * Store the value <code>v</code> in the voxel at the global coordinate <code>(x, y, z)</code>.
     *
     * @param wCoo
     * @param v
     */
    public void setVoxel(WorldCoo wCoo, BlockTypeInfo v) {
        chunkManager.setVoxel(wCoo, v);
    }

    public BlockTypeInfo getVoxel(WorldCoo wCoo) {
        WCVConverter wcv = new WCVConverter(wCoo);
        Chunk chunk = chunkManager.getChunk(wcv.getC());
        if (chunk == null) {
            System.err.println("Tried to load(" + wcv.getC() + ") non-existing chunk");
            return AIR_BLOCK;
        }

        BlockTypeInfo voxel = chunk.getVoxel(wcv.getV());
        return voxel;
    }


    public void ensureMapRegion(ChunkCoo baseCoo) {
        ChunkCoo c = new ChunkCoo();
        for (int x = -ENSURED_REGION_SIZE; x < ENSURED_REGION_SIZE; x++) {
            for (int y = -ENSURED_REGION_SIZE; y < ENSURED_REGION_SIZE; y++) {
                for (int z = -ENSURED_REGION_SIZE; z < ENSURED_REGION_SIZE; z++) {
                    c.assign(baseCoo).addXYZ(x, y, z);
                    chunkManager.getChunk(c);
                }
            }
        }

    }
}
