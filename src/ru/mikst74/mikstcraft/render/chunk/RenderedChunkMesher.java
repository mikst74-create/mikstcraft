package ru.mikst74.mikstcraft.render.chunk;

import ru.mikst74.mikstcraft.meshing.FaceConsumer;
import ru.mikst74.mikstcraft.meshing.GreedyMeshing3;
import ru.mikst74.mikstcraft.model.NeighborCode;
import ru.mikst74.mikstcraft.model.chunk.VoxelField;
import ru.mikst74.mikstcraft.render.RenderedWorldArea;
import ru.mikst74.mikstcraft.settings.GameProperties;
import ru.mikst74.mikstcraft.util.DelayedRunnable;
import ru.mikst74.mikstcraft.util.array.DynamicByteBuffer;
import ru.mikst74.mikstcraft.util.array.FirstFitFreeListAllocator2;
import ru.mikst74.mikstcraft.world.chunk.ChunkManager;

import static ru.mikst74.mikstcraft.model.NeighborCode.forEachNeighborCode;
import static ru.mikst74.mikstcraft.render.buffers.PerFaceBuffers.allocatePerFaceBufferRegion;
import static ru.mikst74.mikstcraft.util.BackgroundExecutor.updateAndRenderRunnables;

/**
 * Created by Mikhail Krinitsyn on 10.01.2026
 */
public class RenderedChunkMesher {
    private ChunkManager      chunkManager;
    private RenderedWorldArea renderedWorldArea;


    public RenderedChunkMesher(ChunkManager chunkManager, RenderedWorldArea renderedWorldArea) {
        this.chunkManager      = chunkManager;
        this.renderedWorldArea = renderedWorldArea;
    }

    /**
     * Mesh the given chunk and schedule writing the faces into buffer objects.
     *
     * @param renderedChunk
     * @return the point in monotonic time when the meshing and appending to vertex/index data byte
     * buffers completed
     */
    public long meshChunkFacesAndWriteToBuffers(RenderedChunk renderedChunk) {

        VoxelField vf = renderedChunk.getChunk().getVoxelField();
vf.recalcAllBitMask();
        if (GameProperties.DEBUG2) {
            System.out.println("meshChunkFacesAndWriteToBuffers: " + renderedChunk + " ");
        }
        DynamicByteBuffer[] vertexData = new DynamicByteBuffer[6];
        DynamicByteBuffer[] indices = new DynamicByteBuffer[6];
        forEachNeighborCode((nc) -> {
            vertexData[nc.getI()] = new DynamicByteBuffer(/*vf.num*/32768 / 4); //TODO понять какой должен быть размер
            indices[nc.getI()]    = new DynamicByteBuffer(/*vf.num*/32768 / 4);
        });
        int faceCount[] = new GreedyMeshing3()
                .mesh(renderedChunk.getChunk(), new FaceConsumer() {
                    private int i[] = new int[6];

                    public void consume(int u0, int v0, int u1, int v1, int p, NeighborCode nc, int v) {
                        appendFaceVertexAndIndexData(renderedChunk, i[nc.getI()]++, u0, v0, u1, v1, p, nc, v, vertexData, indices);
                    }
                });
        FirstFitFreeListAllocator2.Region[] r = new FirstFitFreeListAllocator2.Region[6];
        forEachNeighborCode((nc) -> {
            r[nc.getI()] = allocatePerFaceBufferRegion(faceCount[nc.getI()]);
        });
        long time = System.nanoTime();

        /* Issue render thread task to update the buffer objects */
        updateAndRenderRunnables.add(new DelayedRunnable(() -> {
            forEachNeighborCode((nc) -> {
                int ncI = nc.getI();
                renderedChunk.r[ncI] = r[ncI];
                renderedWorldArea.activeFaceCount += renderedChunk.r[ncI].len;
            });
            renderedWorldArea.getRenderedChunkFaceUpdater().updateChunkVertexAndIndexDataInBufferObjects(renderedChunk, vertexData, indices);
            renderedChunk.lastMeshUpdateTime = System.nanoTime();
            forEachNeighborCode((nc) -> {
                vertexData[nc.getI()].free();
                indices[nc.getI()].free();
            });
            return null;
        }, "Update chunk vertex data", 0));

        return time;
    }

    /**
     * Write vertex/index data for the given face.
     * <p>
     * A face will either be triangulated for GL_TRIANGLE_STRIPS or written as a single point for
     * GL_POINTS rendering.
     *
     * @param renderedChunk
     * @param fi
     * @param u0
     * @param v0
     * @param u1
     * @param v1
     * @param p
     * @param nc
     * @param v
     * @param vertexData
     * @param indices
     */
    public static void appendFaceVertexAndIndexData(RenderedChunk renderedChunk, int fi,
                                                    int u0, int v0,
                                                    int u1, int v1,
                                                    int p, NeighborCode nc,
                                                    int v,
                                                    DynamicByteBuffer[] vertexData,
                                                    DynamicByteBuffer[] indices) {
        /**
         * s - код стороны блока
         * три бита
         * AAS
         * S - 0 - младшая сторона по оси
         *     1 - старшая сторона по оси
         * AA - код оси XYZ
         *    00 - X
         *    01 - Y
         *    10 - Z
         */

        switch (nc) {
            case XP:
            case XM:
                fillPositionsTypesSideAndAoFactorsX(renderedChunk.index, p, u0, v0, u1, v1, nc, v, vertexData);
                break;
            case YP:
            case YM:
                fillPositionsTypesSideAndAoFactorsY(renderedChunk.index, p, u0, v0, u1, v1, nc, v, vertexData);
                break;
            case ZP:
            case ZM:
                fillPositionsTypesSideAndAoFactorsZ(renderedChunk.index, p, u0, v0, u1, v1, nc, v, vertexData);
                break;
        }
        fillIndices(nc, fi, indices);
    }

    /**
     * Write a single short for the given index (when drawing faces as points).
     */
    private static void fillIndex(int i, DynamicByteBuffer indices) {
        indices.putShort(i);
    }

    /**
     * Writes indices of the face at index <code>i</code> for TRIANGLE_STRIP rendering.
     * <p>
     * This will also write a {@link GameProperties#PRIMITIVE_RESTART_INDEX} token.
     *
     * @param nc      the side of the face
     * @param i       the index of the face
     * @param indices will receive the indices for TRIANGLE_STRIP rendering
     */
    private static void fillIndices(NeighborCode nc, int i, DynamicByteBuffer[] indices) {
        if (nc.getP() == 1) { //isPositiveSide
            indices[nc.getI()]
                    .putInt((i << 2) + 1 | (i << 2) + 3 << 16)
                    .putInt(i << 2 | (i << 2) + 2 << 16)
                    .putShort(GameProperties.PRIMITIVE_RESTART_INDEX);
        } else {
            indices[nc.getI()]
                    .putInt((i << 2) + 2 | (i << 2) + 3 << 16)
                    .putInt(i << 2 | (i << 2) + 1 << 16)
                    .putShort(GameProperties.PRIMITIVE_RESTART_INDEX);
        }
    }

    private static boolean isPositiveSide(int side) {
        return (side & 1) != 0;
    }


    /**
     * Write the face position, extents, type, side and ambient occlusion factors for GL_POINTS
     * rendering of an X face.
     * <p>
     * Since the Y coordinate can have values from 0-256, we will reserve 8 bits for Y (which is always
     * encoded first) and 5 bits (for 0-31) for X and Z.
     */
    private static void fillPositionTypeSideAndAoFactorsX(int p, int u0, int v0, int u1, int v1, int s, int n00,
                                                          int n10, int n01, int n11, int v,
                                                          DynamicByteBuffer vertexData) {
        vertexData.putInt(u0 | p << 8 | v0 << 14 | (u1 - u0 - 1) << 20 | (v1 - v0 - 1) << 25).putInt((byte) v | s << 8 | aoFactors(n00, n10, n01, n11) << 16);
    }

    /**
     * Write the face position, extents, type, side and ambient occlusion factors for GL_POINTS
     * rendering of an Y face.
     * <p>
     * Since the Y coordinate can have values from 0-256, we will reserve 8 bits for Y (which is always
     * encoded first) and 5 bits (for 0-31) for X and Z.
     */
    private static void fillPositionTypeSideAndAoFactorsY(int p, int u0, int v0, int u1, int v1, int s, int n00,
                                                          int n10, int n01, int n11, int v,
                                                          DynamicByteBuffer vertexData) {
        vertexData.putInt(p | v0 << 8 | u0 << 14 | (u1 - u0 - 1) << 20 | (v1 - v0 - 1) << 25).putInt((byte) v | s << 8 | aoFactors(n00, n10, n01, n11) << 16);
    }

    /**
     * Write the face position, extents, type, side and ambient occlusion factors for GL_POINTS
     * rendering of a Z face.
     * <p>
     * Since the Y coordinate can have values from 0-256, we will reserve 8 bits for Y (which is always
     * encoded first) and 5 bits (for 0-31) for X and Z.
     */
    private static void fillPositionTypeSideAndAoFactorsZ(int p, int u0, int v0, int u1, int v1, int s, int n00,
                                                          int n10, int n01, int n11, int v,
                                                          DynamicByteBuffer vertexData) {
        vertexData.putInt(v0 | u0 << 8 | p << 14 | (u1 - u0 - 1) << 20 | (v1 - v0 - 1) << 25).putInt((byte) v | s << 8 | aoFactors(n00, n10, n01, n11) << 16);
    }

    private static void fillPositionsTypesSideAndAoFactorsZ(int idx, int p, int u0, int v0, int u1, int v1, NeighborCode nc,
                                                            int v,
                                                            DynamicByteBuffer[] vertexData) {
        int sideAndAoFactors = nc.getMs() | v >>> 8;
        vertexData[nc.getI()].putInt(u0 | v0 << 8 | p << 16 | (byte) v << 24).putShort(sideAndAoFactors).putInt(idx);
        vertexData[nc.getI()].putInt(u1 | v0 << 8 | p << 16 | (byte) v << 24).putShort(sideAndAoFactors).putInt(idx);
        vertexData[nc.getI()].putInt(u0 | v1 << 8 | p << 16 | (byte) v << 24).putShort(sideAndAoFactors).putInt(idx);
        vertexData[nc.getI()].putInt(u1 | v1 << 8 | p << 16 | (byte) v << 24).putShort(sideAndAoFactors).putInt(idx);
    }

    private static void fillPositionsTypesSideAndAoFactorsY(int idx, int p, int u0, int v0, int u1, int v1, NeighborCode nc,
                                                            int v,
                                                            DynamicByteBuffer[] vertexData) {
        int sideAndAoFactors = nc.getMs() | v >>> 8;
        vertexData[nc.getI()].putInt(v0 | p << 8 | u0 << 16 | (byte) v << 24).putShort(sideAndAoFactors).putInt(idx);
        vertexData[nc.getI()].putInt(v0 | p << 8 | u1 << 16 | (byte) v << 24).putShort(sideAndAoFactors).putInt(idx);
        vertexData[nc.getI()].putInt(v1 | p << 8 | u0 << 16 | (byte) v << 24).putShort(sideAndAoFactors).putInt(idx);
        vertexData[nc.getI()].putInt(v1 | p << 8 | u1 << 16 | (byte) v << 24).putShort(sideAndAoFactors).putInt(idx);
    }

    private static void fillPositionsTypesSideAndAoFactorsX(int idx, int p, int u0, int v0, int u1, int v1, NeighborCode nc,
                                                            int v,
                                                            DynamicByteBuffer[] vertexData) {
        // aoFactors(n00, n10, n01, n11) - это один байт, если у стороны есть "выпирающие" соседи, то бит=1, если нет, то =0.
        // n00 - 0x03 - M-M
        // n10 - 0x0C
        // n01 - 0x30
        // n11 - 0xC0
//        ~0x0CFF;

        int sideAndAoFactors = nc.getMs() | v >>> 8;// | aoFactors(n00, n10, n01, n11) << 8;
        /**
         * структура vertexData для chunk.vs.glsl
         *  первые 4ре байта позиция вертекса
         *         layout(location=0) in uvec4 positionAndType;
         *              0 байт: координата внутри чанка по плоскости (p) (4 бита)
         *              1 байт: координата u (в пределах чанка) (4 бита)
         *              2 байт: координата v (в пределах чанка) (4 бита)
         *              3 байт: id текстуры (8 бит) , а нужно бы 10
         *
         *         layout(location=1) in uvec2 sideAndAoFactors;
         *              0 байт: сторона куба (6 бит)
         *              1 байт: AO фактор (8 бит)
         *         layout(location=2) in uint chunkIndex;
         *              0-1 байт: индекс чанка (16 бит), нужен для определения сдвига всех вертексов чанка
         */


        vertexData[nc.getI()].putInt(p | u0 << 8 | v0 << 16 | (byte) v << 24).putShort(sideAndAoFactors).putInt(idx);
        vertexData[nc.getI()].putInt(p | u1 << 8 | v0 << 16 | (byte) v << 24).putShort(sideAndAoFactors).putInt(idx);
        vertexData[nc.getI()].putInt(p | u0 << 8 | v1 << 16 | (byte) v << 24).putShort(sideAndAoFactors).putInt(idx);
        vertexData[nc.getI()].putInt(p | u1 << 8 | v1 << 16 | (byte) v << 24).putShort(sideAndAoFactors).putInt(idx);
    }

    /**
     * Compute the ambient occlusion factor from a vertex's neighbor configuration <code>n</code>.
     */
    private static int aoFactor(int n) {
        return (n & 1) == 1 && (n & 4) == 4 ? 0 : 3 - Integer.bitCount(n);
    }

    /**
     * Encode the four ambient occlusion factors into a single byte.
     */
    private static byte aoFactors(int n00, int n10, int n01, int n11) {
        return (byte) (aoFactor(n00) | aoFactor(n10) << 2 | aoFactor(n01) << 4 | aoFactor(n11) << 6);
    }
}
