package ru.mikst74.mikstcraft.render;

import lombok.Getter;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import ru.mikst74.mikstcraft.model.NeighborCode;
import ru.mikst74.mikstcraft.model.camera.Camera;
import ru.mikst74.mikstcraft.model.chunk.Chunk;
import ru.mikst74.mikstcraft.model.coo.ChunkCoo;
import ru.mikst74.mikstcraft.model.coo.WCVConverter;
import ru.mikst74.mikstcraft.model.coo.WorldCoo;
import ru.mikst74.mikstcraft.render.buffers.PerFaceBuffers;
import ru.mikst74.mikstcraft.render.chunk.*;
import ru.mikst74.mikstcraft.render.model.FrustumPlanes;
import ru.mikst74.mikstcraft.settings.GameProperties;
import ru.mikst74.mikstcraft.util.array.ThreeDimensionUnlimitedField;
import ru.mikst74.mikstcraft.world.WorldMap;
import ru.mikst74.mikstcraft.world.chunk.ChunkDistance;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.lang.Math.floor;
import static org.lwjgl.system.MemoryStack.stackPush;
import static ru.mikst74.mikstcraft.model.coo.VoxelCoo.CHUNK_SIZE;
import static ru.mikst74.mikstcraft.settings.GameProperties.DEBUG2;
import static ru.mikst74.mikstcraft.settings.OpenGLProperties.drawPointsWithGS;
import static ru.mikst74.mikstcraft.settings.OpenGLProperties.useMultiDrawIndirect;
import static ru.mikst74.mikstcraft.util.time.Profiler.profile;

/*
  Хранит видимую часть карты, круг из чанков диаметром MAX_RENDER_DISTANCE_METERS, как массивы RenderedChunk (чанки с подготовленными данными для ренедринга)
 */
public class RenderedWorldArea {
    // Центр области карты в координатах чанков
    @Getter
    private       Vector3f position;
    private final Vector3i iPosition;

    // 3D массив видимых чанков
    @Getter
    private final ThreeDimensionUnlimitedField<RenderedChunk> mapByCoo = new ThreeDimensionUnlimitedField<>();

    // Линейный лист всех видимых чанков
    @Getter
    private final List<RenderedChunk> allRenderedChunks = new ArrayList<>();


    // Линейный лист всех чанков на границе видимости (то есть тех, у кого нет видимых соседей)
    @Getter
    public final List<RenderedChunk> frontierChunks = new ArrayList<>();

    // позиция центра области на предыдущем кадре (вынести в WorldAreaRenderer)
    private final ChunkCoo     lastPosition = new ChunkCoo(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    private final WCVConverter wcv          = new WCVConverter();
    // Пока магия
//    private final Comparator<RenderedChunk> inView            = comparing(this::chunkNotInFrustum);
//    private final Comparator<RenderedChunk> byDistance        = comparingDouble(c -> ChunkDistance.distToChunk(c.getChunk().getCoo()));
//    private final Comparator<RenderedChunk> inViewAndDistance = inView.thenComparing(byDistance);

    @Getter
    private final FrustumPlanes frustumPlanes = new FrustumPlanes();

    /**
     * Объект МИР с полной картой чанков
     */
    private final WorldMap worldMap;

    @Getter
    private final ChunkRenderer chunkRenderer;
    /**
     * Simple {@link BitSet} to find and allocate indexes for per-chunk arrays/buffers.
     */
    @Getter
    private final BitSet        chunkIndexes         = new BitSet(GameProperties.MAX_ACTIVE_CHUNKS);
    public        int           activeFaceCount;
    /**
     * The number of chunk building tasks that are currently queued and have not yet finished.
     */
    @Getter
    public final  AtomicInteger chunkBuildTasksCount = new AtomicInteger();

    // Помощники
    private       RenderedChunkCreator     renderedChunkCreator;
    private       RenderedChunkDestroyer   renderedChunkDestroyer;
    @Getter
    private       RenderedChunkFaceUpdater renderedChunkFaceUpdater;
    @Getter
    private final RenderedChunkMesher      renderedChunkMesher;
    private final VisibilityManager        visibilityManager;
    @Getter
    private       int                      inFrustrum;
    private       boolean                  wireframe;
    private       int                      materialsTexture;
    public        int                      indirectDrawBuffer;
    private       long                     indirectDrawBufferAddr;

    private final ExecutorService executorService;

    public RenderedWorldArea(WorldMap worldMap) {
        this.worldMap            = worldMap;
        this.position            = new Vector3f();
        this.iPosition           = new Vector3i();
        renderedChunkCreator     = new RenderedChunkCreator(this);
        renderedChunkFaceUpdater = new RenderedChunkFaceUpdater(this);
        renderedChunkMesher      = new RenderedChunkMesher(worldMap.getChunkManager(), this);
        renderedChunkDestroyer   = new RenderedChunkDestroyer(this);
        visibilityManager        = new VisibilityManager(this::getIfExists);
        chunkRenderer            = new ChunkRenderer();
        /*
         * Compute number of vertices per face and number of bytes per vertex. These depend on the features
         * we are going to use.
         */
        PerFaceBuffers.verticesPerFace = drawPointsWithGS ? 1 : 4;
        PerFaceBuffers.indicesPerFace  = drawPointsWithGS ? 1 : 5;
        PerFaceBuffers.voxelVertexSize = drawPointsWithGS ? 2 * Integer.BYTES : Integer.BYTES + Short.BYTES + (!useMultiDrawIndirect ? Integer.BYTES : 0);

        PerFaceBuffers.createChunkInfoBuffers();
        executorService = Executors.newFixedThreadPool(10);
    }

    public void linkToCamera(Camera camera) {
        position = camera.getPosition();
        chunkRenderer.setMvp(camera.getMvp());
        chunkRenderer.setPosition(camera.getPosition());
    }

    public void createInRenderDistanceAndDestroyOutOfRenderDistanceChunks() {
        wcv.assign(position);

//        if (wcv.getC().equals(lastPosition)) {
//            return;
//        }
        lastPosition.assign(wcv.getC());

        boolean res = true;
        while (res) {
            destroyOutOfRenderDistanceFrontierChunks();
            /* Then, create tasks for new chunks to be created */
            res = createNewInRenderDistanceFrontierChunks(wcv.getC());
        }
    }

    /**
     * Based on the current frontier chunks, check if any of their four neighbors is within the
     * {@link GameProperties#MAX_RENDER_DISTANCE_CHUNKS} and need to be created.
     *
     * @return <code>true</code> if any new chunks have been created; <code>false</code> otherwise
     */
    public boolean createNewInRenderDistanceFrontierChunks(ChunkCoo coo) {

        /* Then check for new chunks to generate */
        worldMap.ensureMapRegion(coo);
        /* first - if no frontier: it is we have an empty world area. Need to add first chunk */
        if (frontierChunks.isEmpty()) {
            ensureChunkIfVisible(coo);
        }

        if (1 == 1) {
//            frontierChunks.sort(inViewAndDistance);
            boolean newChunkCreated = false;
            ChunkCoo tmpCoo = new ChunkCoo();
            for (int i = 0, frontierChunksSize = frontierChunks.size(); i < frontierChunksSize && !newChunkCreated; i++) {
                /* iterate index-based because we modify the list by appending new elements at the end! */
                RenderedChunk fc = frontierChunks.get(i);
//            if (worldMap.getChunkHolder().chunkBuildTasksCount.get() >= GameProperties.MAX_NUMBER_OF_CHUNK_TASKS) {
//                break;
//            }
                ChunkCoo fcCoo = fc.getChunk().getCoo();
                for (NeighborCode nc : NeighborCode.values()) {
                    newChunkCreated = ensureChunkIfVisible(tmpCoo.assign(fcCoo).step(nc));
//                newChunkCreated = false;//надо бы с этой механикой разобраться/понятно что она для плавности игры, но чего все так тормозит то)))
//                    return newChunkCreated;
                }
            }
        }
        return false;
    }

    /**
     * Iterate through all current frontier chunks and check, whether any of them is further than the
     * {@link GameProperties#MAX_RENDER_DISTANCE_CHUNKS} aways, in which case those will be destroyed.
     */
    public void destroyOutOfRenderDistanceFrontierChunks() {

        frontierChunks.stream().filter(rc -> !ChunkDistance.chunkInRenderDistance(position, rc.getChunk().getCoo())).collect(Collectors.toList()).stream().forEach(rc -> {
            if (GameProperties.DEBUG) {
                System.out.println("Frontier chunk is not in view anymore: " + rc);
            }
            System.out.println("Frontier chunk is not in view anymore: " + rc);

            renderedChunkDestroyer.destroyChunk(rc);
        });
    }

    /**
     * Determine whether the given chunk does <i>not</i> intersect the view frustum.
     */
    public boolean chunkNotInFrustum(WorldCoo wcoo) {
//        WorldCoo coo111=new WorldCoo(renderedChunk.getWCoo());
        float xf = (wcoo.getX()) - (float) floor(position.x);
        float yf = (wcoo.getY()) - (float) floor(position.y);
        float zf = (wcoo.getZ()) - (float) floor(position.z);
        Matrix4f m=new Matrix4f(chunkRenderer.getMvp());
        m.translate(chunkRenderer.getPosition());
        FrustumIntersection fi = new FrustumIntersection(m);
        return !(fi.testPoint(xf, yf, zf) || fi.testPoint(xf + CHUNK_SIZE, yf + CHUNK_SIZE, zf + CHUNK_SIZE));
//        return frustumPlanes.culledXY(xf, yf, zf, xf + CHUNK_SIZE, yf + CHUNK_SIZE, zf + CHUNK_SIZE);
    }

    /**
     * Determine whether the given chunk does <i>not</i> intersect the view frustum.
     */
    public boolean chunkNotInFrustum(ChunkCoo coo) {
//        WorldCoo coo111=new WorldCoo(renderedChunk.getWCoo());
        float xf = (coo.getX() * CHUNK_SIZE) - (float) floor(position.x);
        float yf = (coo.getY() * CHUNK_SIZE) - (float) floor(position.y);
        float zf = (coo.getZ() * CHUNK_SIZE) - (float) floor(position.z);
        Matrix4f m=new Matrix4f(chunkRenderer.getMvp());
        m.translate(chunkRenderer.getPosition());
        FrustumIntersection fi = new FrustumIntersection(m);
        return !(fi.testPoint(xf, yf, zf) || fi.testPoint(xf + CHUNK_SIZE, yf + CHUNK_SIZE, zf + CHUNK_SIZE));
//        return frustumPlanes.culledXY(xf, yf, zf, xf + CHUNK_SIZE, yf + CHUNK_SIZE, zf + CHUNK_SIZE);
    }

    public RenderedChunk getIfExists(ChunkCoo coo) {
        return mapByCoo.get(coo);
    }

    private boolean ensureChunkIfVisible(ChunkCoo coo) {
        if (mapByCoo.containsKey(coo)) {
            return false;
        }
        if (chunkNotInFrustum(coo)) {
//            System.out.println("chunkNotInFrustum " + coo);
            return false;
        }

        if (!ChunkDistance.chunkInRenderDistance(position, coo)) {
            return false;
        }
        Chunk c = worldMap.getChunkManager().getChunk(coo);
        RenderedChunk rc = renderedChunkCreator.create(c);
        mapByCoo.set(coo, rc);
        frontierChunks.add(rc);
        allRenderedChunks.add(rc);
//        executorService.submit(() -> {

//            renderedChunkMesher.meshChunkFacesAndWriteToBuffers(rc);
//            ChunkNeighborSupplier.ensureNeighbor(c);
            renderedChunkMesher.meshChunkFacesAndWriteToBuffers(rc);
            if (GameProperties.DEBUG2) {
                System.out.println("New frontier neighbor chunk is in view: " + rc);
            }
//        });
        return true;
    }

    public void rebuildMeshForUpdatedChunks() {
        allRenderedChunks.stream()
                .filter(c -> c.lastMeshUpdateTime < c.getChunk().getLastUpdateTime())
                .forEach(renderedChunkFaceUpdater::updateChunk);
    }

    /**
     * Draw the voxels via the non-MDI render path with glMultiDrawElementsBaseVertex().
     *
     */
    public void drawChunksWithMultiDrawElementsBaseVertex() {
        if (PerFaceBuffers.chunksVao == 0) {
            return;
        }
//        preDrawChunksState();
        try (MemoryStack stack = stackPush()) {
            List<RenderedChunk> allChunk = getAllRenderedChunks();
            int size = allChunk.size() * 6;
            PointerBuffer indices = stack.mallocPointer(size);
            IntBuffer count = stack.mallocInt(size);
            IntBuffer basevertex = stack.mallocInt(size);
            AtomicLong total = new AtomicLong();
            inFrustrum = 0;
            for (RenderedChunk c : allChunk) {
//                if(Math.random()>0.1f){continue;}
                if (!c.ready || chunkNotInFrustum(c.getWCoo())) {
                    c.inFrustrum = false;
                    continue;
                }
                c.inFrustrum = true;
                c.isVisible  = true;
                inFrustrum++;
            }
            wcv.assign(position);
            profile("visibilityManager", () -> visibilityManager.computeVisibleChunks(wcv.getC()));
            List<RenderedChunk> renderedChunks = allChunk.stream()
                    .filter(rc -> rc.isVisible) // filter only visible chunks
                    .collect(Collectors.toList());
//            System.out.println("renderedChunks length " + renderedChunks.size());
            for (RenderedChunk c : renderedChunks) {

                for (NeighborCode nc : c.getRenderedFaces(position)) {
                    if (!c.isReady()) {
                        continue;
                    }
                    int ncI = nc.getI();
                    indices.put((long) Short.BYTES * c.r[ncI].off * PerFaceBuffers.indicesPerFace);
                    count.put(c.r[ncI].len * PerFaceBuffers.indicesPerFace);
                    basevertex.put(c.r[ncI].off * PerFaceBuffers.verticesPerFace);
                    total.addAndGet((long) Short.BYTES * c.r[ncI].len * PerFaceBuffers.indicesPerFace);
                }
            }
            indices.flip();
            count.flip();
            basevertex.flip();
//            updateChunksProgramUbo();
            if (DEBUG2) {
                System.out.println("draw count: " + total);
            }
//            profile("glMultiDrawElementsBaseVertex", () -> {
//                glMultiDrawElementsBaseVertex(GL_TRIANGLE_STRIP, count, GL_UNSIGNED_SHORT, indices, basevertex);
//                glMultiDrawElementsBaseVertex(GL_POINT, count, GL_UNSIGNED_SHORT, indices, basevertex);
//            });
            chunkRenderer.render(count, indices, basevertex);
        }
    }

//
//
//    /**
//     * Create the (multi-buffered) buffer for multi-draw-indirect rendering, holding either the MDI draw
//     * structs of chunks or, if we use occlusion culling, hold the chunks' face offset and count.
//     */
//    public void createMultiDrawIndirectBuffer() {
//        indirectDrawBuffer = glGenBuffers();
//        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, indirectDrawBuffer);
//        /*
//         * When we use occlusion culling (temporal coherence or not) we do not store MDI structs into the
//         * indirect buffer, but only face offset and count, because we will generated the actual MDI structs
//         * in the collectdrawcalls.vs.glsl shader.
//         */
//        long structSize = useOcclusionCulling ? 2 * Integer.BYTES : 5 * Integer.BYTES;
//        if (useBufferStorage) {
//            glBufferStorage(GL_DRAW_INDIRECT_BUFFER, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * GameProperties.MAX_ACTIVE_CHUNKS * structSize,
//                    GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT);
//            indirectDrawBufferAddr = nglMapBufferRange(GL_DRAW_INDIRECT_BUFFER, 0L, GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * GameProperties.MAX_ACTIVE_CHUNKS * structSize,
//                    GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_FLUSH_EXPLICIT_BIT);
//        } else {
//            glBufferData(GL_DRAW_INDIRECT_BUFFER, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * GameProperties.MAX_ACTIVE_CHUNKS * structSize, GL_STATIC_DRAW);
//        }
//    }

}
