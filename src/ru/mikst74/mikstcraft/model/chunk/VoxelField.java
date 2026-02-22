package ru.mikst74.mikstcraft.model.chunk;

import lombok.Getter;
import lombok.Setter;
import ru.mikst74.mikstcraft.dictionary.BlockTypeDictionary;
import ru.mikst74.mikstcraft.dictionary.BlockTypeInfo;
import ru.mikst74.mikstcraft.model.NeighborCode;
import ru.mikst74.mikstcraft.model.coo.VoxelCoo;
import ru.mikst74.mikstcraft.model.coo.VoxelCooIteratorXAxis;
import ru.mikst74.mikstcraft.util.floodfill.ChunkVisibility;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.mikst74.mikstcraft.dictionary.BlockTypeDictionary.AIR_BLOCK;
import static ru.mikst74.mikstcraft.model.NeighborCode.*;
import static ru.mikst74.mikstcraft.model.chunk.VoxelFieldAoFactorsMatrix.AO_FACTOR_MATRIX;
import static ru.mikst74.mikstcraft.model.coo.CooConstant.*;
import static ru.mikst74.mikstcraft.util.DebugHelper.format32BitLongAs64String;
import static ru.mikst74.mikstcraft.util.DebugHelper.format64BitLongAs64String;
import static ru.mikst74.mikstcraft.util.math.ExtMath.doubleBits;
import static ru.mikst74.mikstcraft.util.time.Profiler.profile;

/**
 * Represents the voxel field of a single chunk.
 * <p>
 * VoxelField is a cube 18x18x18. In center data of current chuck (16x16x16) and one cell outside on each side - data from neighbor chunks (total 26 neighbor)
 *
 */
@Getter
public class VoxelField implements Serializable {
    public static final int VISIBLE_SIEVE_MASK = 0x55555555;
    public static final int EQUALS_SIEVE_MASK  = ~VISIBLE_SIEVE_MASK;

    /**
     * Size of VoxelField object
     * 0. VoxelField headers: 56 bytes
     * 1. Field. 18*18*18*2 = 11664 bytes
     * 2. Solid and Glue masks: 6*16*16=1536 bytes x2 times = 3072 bytes
     * 3. Ao array. 16*16*16*6*1 = 4096
     * <p>
     * total: 26456 bytes. Can be 14792 (if make field as short[])
     */
    //
    public static final int FIELD_ARRAY_SIZE         = (1 + 16 + 1) * (1 + 16 + 1) * (1 + 16 + 1); // Размер чанка 16х16х16 + 1 с каждой стороны для копирования данных соседнего чанка
    public static final int FIELD0_ARRAY_SIZE        = 16 * 16 * 16; // Размер чанка 16х16х16 + 1 с каждой стороны для копирования данных соседнего чанка
    // 6*16*16=1536 bytes
    public static final int SOLID_BITMASK_ARRAY_SIZE = 6 * 18 * 18;
    public static final int MESH_DATA_ARRAY_SIZE     = 6 * 16 * 16;

    /**
     * Порядок обхода слоев для расчета битовых масок
     * значение для массива - номер оси
     */
    public static final int[]          U = {X_AXIS, Y_AXIS, Z_AXIS};
    public static final int[]          V = {Y_AXIS, Z_AXIS, X_AXIS};
    public static final int[]          A = {Z_AXIS, X_AXIS, Y_AXIS};
    public static final NeighborCode[] N = {XP, YP, ZP};
    /**
     * The actual voxel field as a flat array.
     * Value is blockID (See @BlockTypeDictionary)
     */
    private final       short[]        field;

    /**
     * Bit mask with glued faces of solid blocks (see bit mask for 'field')
     * 0 - faces is glued, not visible, no render
     * 1 - self or neighbor faces is not solid, may be visible
     * One value is bit mask for a row of voxels.
     * <p>
     * +-+-+-+ ... +-+-+
     * | | | | ... | | |
     * +-+-+-+ ... +-+-+
     * ...        ...
     * | | | | ... | | |  <- one row of 16 voxel is one int value (16bit)
     * +-+-+-+ ... +-+-+
     * <p>
     * <p>
     *
     *     All bit mask have 6 slices, 2per one axis 0 side (left) and  +1 side (right)
     *     0,1. XY mask along Z
     *     2,3. YZ mask along X
     *     4,5. ZX mask along Y
     *
     */

    /**
     * 1 bit per face.
     * 0 - face is not solid (= no face)
     * 1- face is solid,
     * <p>
     * Для вокселей текущего чанка используются байты 1 и 2. Старший бит 0 байта и младший бит 3 байта - маска по данным вокселей соседних чанков
     */
    private transient final int[] solidFaceField;
    /**
     * 0 - face is glued, not visible
     * 1 - face is open, visible
     *
     */
    private transient final int[] visibleFaceField;
    /**
     * 0 - current and next voxel have different texture
     * 1 - current and next voxel have equal texture
     */
    private transient final int[] textureEqualsWithNextFaceField;
    /**
     * 0 - current and next voxel have different aoFactor
     * 1 - current and next voxel have equal aoFactor
     */
    private transient final int[] aoEqualsWithNextFaceField;

    /**
     * "visibility" or "transparency" through chuck for raytrace. Use for occlusing whole chuck
     * Each byte is NeighborCode from and value is byte mask which faces are connected with "from" face.
     */
    private final transient ChunkVisibility.VisibilityResult airFlowThroughChunk;

    /**
     * 8 bit per face.
     * <p>
     * +----+----+----+
     * | a3 | a4 | a5 |
     * +----+----+----+
     * | a2 | UV | a6 |
     * +----+----+----+
     * | a1 | a0 | a7 |
     * +----+----+----+
     * <p>
     * Если рядом присутствуют блоки выше текущего, то нужно затенить грань. 4 стороны, 4 угла - 8бит.
     * 0 - затенять не нужно
     * 1 - затенить нужно
     * <p>
     * Для сторон a0 a2 a4 a6 - достаточно проверить, что примыкающая к UV сторона solid
     * для углов 1 проставляется если хотя бы одна из примыкающих к углу сторон - solid
     * Итого 12 проверок
     */
    private transient final byte[] aoFactorFaceField;

    /**
     * 2 bit per face.
     * Low bit - 0 - no face/no solid face, 1 - solid face
     * High bit- 0 - next voxel in row has another texture or AO factor, 1 - next voxel in row has the same texture as the current one and zero AO
     * 00 - no face generate need
     * 01 - need draw face, next face has another texture
     * 11 - need draw, next face has the same texture
     * 10 - no face generate need, but has the same texture (for example, a cut-out stair block)
     */
    private transient final int[] meshingDataField;

    private transient boolean  isLoadingMode;
    @Setter
    private transient Runnable updateCallback;

    private transient int solidBlockCount;

    private transient long    lastUpdateTime;
//    private transient boolean bitMasksIsOutOfDate;

    /**
     * The number of set/active non-zero voxels. This value can be used to get a (very) rough estimate
     * of the needed faces when meshing.
     */
//    public int num;
    public VoxelField() {
        this.field                          = new short[FIELD_ARRAY_SIZE];
        this.solidFaceField                 = new int[SOLID_BITMASK_ARRAY_SIZE];
        this.visibleFaceField               = new int[SOLID_BITMASK_ARRAY_SIZE];
        this.textureEqualsWithNextFaceField = new int[SOLID_BITMASK_ARRAY_SIZE];
        this.aoEqualsWithNextFaceField      = new int[SOLID_BITMASK_ARRAY_SIZE];
        this.meshingDataField               = new int[MESH_DATA_ARRAY_SIZE];
        this.aoFactorFaceField              = new byte[6 * FIELD0_ARRAY_SIZE];
//        for (int i = 0; i < 6 * FIELD0_ARRAY_SIZE; i++) {
//            aoFactorFaceField[i] = (byte) 0xff;
//        }

        this.airFlowThroughChunk = new ChunkVisibility.VisibilityResult();
        this.solidBlockCount     = 0;
    }


    public int[] getCopyOfGluedFaceField() {
        int[] res = new int[MESH_DATA_ARRAY_SIZE];
//        if (bitMasksIsOutOfDate) {
//            recalcAllBitMask();
//        }
        System.arraycopy(meshingDataField, 0, res, 0, MESH_DATA_ARRAY_SIZE);
        return res;
    }

    public void actualizeUpdateTime() {
        lastUpdateTime = System.nanoTime();
    }

    /**
     * Stores the value 'v' into voxel (x, y, z).
     *
     * @param coo the local coordinate
     * @param v   the voxel value
     * @return this
     */

    public VoxelField store(VoxelCoo coo, BlockTypeInfo v) {
        field[coo.idx()] = (short) v.getId();
        actualizeUpdateTime();
        if (!isLoadingMode) {
            recalcAllBitMask();

            if (updateCallback != null) {
                updateCallback.run();
            }
        }
        return this;
    }

    private void debug(String name, long v) {
        if (1 == 0 && !isLoadingMode) {
            System.out.println("l:" + format64BitLongAs64String(v) + " <- " + name);
        }
    }

    private void debug(String name, int v) {
        if (1 == 0 && !isLoadingMode) {
            System.out.println("i:" + format32BitLongAs64String(v) + " <- " + name);
        }
    }


    /**
     * Loads the current value of the voxel (x, y, z).
     *
     * @return the voxel value
     */


    public int load(VoxelCoo coo) {
        return field[coo.idx()];
    }

    public BlockTypeInfo loadBTI(VoxelCoo coo) {
        short id = field[coo.idx()];
        if (id == 0) {
            return AIR_BLOCK;
        }
        BlockTypeInfo blockTypeInfo = BlockTypeDictionary.getBlockTypeInfo(id);
        return blockTypeInfo == null ? AIR_BLOCK : blockTypeInfo;
    }

    public int getFaceInfo(VoxelCoo coo, NeighborCode nc) {
        /**
         * low two bytes (0,1) - texture Id
         * 2 byte - ao factors
         * 3 byte reserved
         */
        short blockTypeId = field[coo.idx()];
        int aoFactor = aoFactorFaceField[(nc.getI() << 12) + coo.idx0()];
        return blockTypeId | aoFactor << 16;
    }

    public void uploadFieldFrom(VoxelField loadedVoxelField) {
        enableLoadingMode();
        System.arraycopy(loadedVoxelField.field, 0, this.field, 0, FIELD_ARRAY_SIZE);
        actualizeUpdateTime();
        recalcAllBitMask();
        disableLoadingMode();
    }

    public void recalcAllBitMask() {
        //  X => YZ, для nc=XP,XM , U=X, V=Y, биты маски вдоль Z
        //  Y => ZX, для nc=YP,YM , U=Y, V=Z, биты маски вдоль X
        //  Z => XY, для nc=ZP,ZM , U=Z, V=X, биты маски вдоль Y

        // Axis X
        profile("recalcSolidAndGlueTextureBitMaskAxis", () -> {
            recalcSolidAndGlueTextureBitMaskAxis(X_AXIS);
            recalcSolidAndGlueTextureBitMaskAxis(Y_AXIS);
            recalcSolidAndGlueTextureBitMaskAxis(Z_AXIS);
        });
        profile("recalcGlueFacesBitMaskAxis", () -> {
            recalcGlueFacesBitMaskAxis(X_AXIS);
            recalcGlueFacesBitMaskAxis(Y_AXIS);
            recalcGlueFacesBitMaskAxis(Z_AXIS);
        });
        profile("recalcVoxelsAoFactors", () -> {
            recalcVoxelsAoFactors();
        });
        profile("recalcAoEqualsBitMaskAxis", () -> {
            recalcAoEqualsBitMaskAxis(X_AXIS);
            recalcAoEqualsBitMaskAxis(Y_AXIS);
            recalcAoEqualsBitMaskAxis(Z_AXIS);
        });
        profile("recalcMeshData", () -> {
            recalcMeshData(X_AXIS);
            recalcMeshData(Y_AXIS);
            recalcMeshData(Z_AXIS);
        });
        profile("calcAirFlowThroughChunk()", () -> calcAirFlowThroughChunk());

//        bitMasksIsOutOfDate = false;
        if (updateCallback != null) {
            updateCallback.run();
        }
    }

    public void recalcAoEqualsBitMaskAxis(int axis) {
        VoxelCoo c = new VoxelCoo();
        VoxelCoo cF = new VoxelCoo(); // one step forward from c
        final int ITERATE_AXIS_U = U[axis];
        final int ITERATE_AXIS_V = V[axis];
        final int ALONG_AXIS = A[axis];
        NeighborCode ncRight = N[axis];
        NeighborCode ncLeft = ncRight.getOpposite();
        int aoMaskR;
        int aoMaskL;
        int aoBitValue;
        int currentAoR;
        int currentAoL;
        int nextAoR;
        int nextAoL;
        int aoCooIndex;
        int aoSliceRIndex;
        int aoSliceLIndex;
        while (c.iterate(ITERATE_AXIS_U)) {
            while (c.iterate(ITERATE_AXIS_V)) {
                currentAoR = aoFactorFaceField[(ncRight.getI() << 12) + c.idx0()];
                currentAoL = aoFactorFaceField[(ncLeft.getI() << 12) + c.idx0()];
                nextAoR    = 0;
                nextAoL    = 0;
                aoMaskR    = 0;
                aoMaskL    = 0;
                aoBitValue = 1 << 8;
                int aoAxisUindex = (c.get(ITERATE_AXIS_U) + 1) * 18;

                while (c.iterate(ALONG_AXIS)) {
                    cF.assign(c).inc(ALONG_AXIS);
                    if (cF.get(ALONG_AXIS) < 15) {
                        nextAoR = aoFactorFaceField[(ncRight.getI() << 12) + cF.idx0()];
                        nextAoL = aoFactorFaceField[(ncLeft.getI() << 12) + cF.idx0()];
                    }
                    aoMaskR |= currentAoR == nextAoR ? aoBitValue : 0;
                    aoMaskL |= currentAoL == nextAoL ? aoBitValue : 0;

                    currentAoR = nextAoR;
                    currentAoL = nextAoL;
                    aoBitValue <<= 1;
                }
                aoCooIndex                               = aoAxisUindex + (c.get(ITERATE_AXIS_V) + 1);
                aoSliceRIndex                            = (ncRight.getI324()) + aoCooIndex;
                aoSliceLIndex                            = (ncLeft.getI324()) + aoCooIndex;
                aoEqualsWithNextFaceField[aoSliceRIndex] = aoMaskR;
                aoEqualsWithNextFaceField[aoSliceLIndex] = aoMaskL;
            }
        }
    }
    // 0  -1  -1  -5 -69
    //     0   1   0   0

    public void recalcMeshData(int axis) {
        VoxelCoo c = new VoxelCoo();
        final int ITERATE_AXIS_U = U[axis];
        final int ITERATE_AXIS_V = V[axis];
        final int ALONG_AXIS = A[axis];
        NeighborCode ncRight = N[axis];
        NeighborCode ncLeft = ncRight.getOpposite();
        while (c.iterate(ITERATE_AXIS_U)) {
            while (c.iterate(ITERATE_AXIS_V)) {
                int meshIndex = (c.get(ITERATE_AXIS_U) << 4) | c.get(ITERATE_AXIS_V);
                int sliceUVIndex = (c.get(ITERATE_AXIS_U) + 1) * 18 + (c.get(ITERATE_AXIS_V) + 1);
                int sliceRIndex = (ncRight.getI324()) + sliceUVIndex;
                int sliceLIndex = (ncLeft.getI324()) + sliceUVIndex;
                int visibleMaskR = (int) doubleBits((visibleFaceField[sliceRIndex] >>> 8) & 0xFFFF);
                int visibleMaskL = (int) doubleBits((visibleFaceField[sliceLIndex] >>> 8) & 0xFFFF);
                int equalsMaskR = (int) doubleBits(((textureEqualsWithNextFaceField[sliceRIndex] & aoEqualsWithNextFaceField[sliceRIndex]) >>> 8) & 0xFFFF);
                int equalsMaskL = (int) doubleBits(((textureEqualsWithNextFaceField[sliceLIndex] & aoEqualsWithNextFaceField[sliceLIndex]) >>> 8) & 0xFFFF);
                meshingDataField[(ncRight.getI() << 8) | meshIndex] = (visibleMaskR & VISIBLE_SIEVE_MASK) | (equalsMaskR & EQUALS_SIEVE_MASK);
                meshingDataField[(ncLeft.getI() << 8) | meshIndex]  = (visibleMaskL & VISIBLE_SIEVE_MASK) | (equalsMaskL & EQUALS_SIEVE_MASK);
            }
        }
    }

    public void recalcBitMaskAxis(int axis) {
        //  X => YZ, для nc=XP,XM , U=X, V=Y, биты маски вдоль Z
        /**
         */

        VoxelCoo c = new VoxelCoo();
        VoxelCoo cF = new VoxelCoo(); // one step forward from c
        VoxelCoo cR = new VoxelCoo(); // one step right from c
        VoxelCoo cL = new VoxelCoo(); // one step left from c
        NeighborCode ncRight = N[axis];
        NeighborCode ncLeft = ncRight.getOpposite();
        final int ITERATE_AXIS_U = U[axis];
        final int ITERATE_AXIS_V = V[axis];
        final int ALONG_AXIS = A[axis];
        while (c.iterate(ITERATE_AXIS_U)) {
            while (c.iterate(ITERATE_AXIS_V)) {
                int solidBitMaskP = 0;
                int solidBitMaskM = 0;
                int glueBitMaskP = 0;
                int glueBitMaskM = 0;
                BlockTypeInfo currentVoxel = loadBTI(c);
                int currentAoP = currentVoxel.isSolidFace(ncRight) ? calcFaceAoFactor(c, ncRight) : 0;
                int currentAoM = currentVoxel.isSolidFace(ncLeft) ? calcFaceAoFactor(c, ncLeft) : 0;
                int nextAoP = 0;
                int nextAoM = 0;
                while (c.iterate(ALONG_AXIS)) {
                    cF.assign(c).inc(ALONG_AXIS);
                    cR.assign(c).inc(ITERATE_AXIS_U);// c.getX() + 1, c.getY(), c.getZ());
                    cL.assign(c).dec(ITERATE_AXIS_U);//assign(c.getX() - 1, c.getY(), c.getZ());
                    int shift = c.get(ALONG_AXIS);
                    int isSolidP = currentVoxel.ifSolidFace(ncRight, 1);
                    int isSolidM = currentVoxel.ifSolidFace(ncLeft, 1);
                    solidBitMaskP = (isSolidP << shift) | solidBitMaskP;
                    solidBitMaskM = (isSolidM << shift) | solidBitMaskM;

                    /**
                     * calculate glue bit for current block, right and left faces
                     */
                    int glueP = isSolidP == 1 ? 1 & (isSolidP & ~loadBTI(cR).ifSolidFace(ncLeft, 1)) : 0;
                    int glueM = isSolidM == 1 ? 1 & (isSolidM & ~loadBTI(cL).ifSolidFace(ncRight, 1)) : 0;


                    /**
                     *  Calculate AO factor for current face
                     *  для каждой стороны оси (0 - M и 1 - P) нужно получить 8 значений солид текстур окружающих ее вокселей
                     */
//                    if (glueP == 1) {
                    nextAoP                                              = c.isMax(ALONG_AXIS) ? 0 : calcFaceAoFactor(cF, ncRight);
                    aoFactorFaceField[(ncRight.getI() << 12) + c.idx0()] = (byte) currentAoP;
//                    }
//                    if (glueM == 1) {
                    nextAoM                                             = c.isMax(ALONG_AXIS) ? 0 : calcFaceAoFactor(cF, ncLeft);
                    aoFactorFaceField[(ncLeft.getI() << 12) + c.idx0()] = (byte) currentAoM;
//                    }
                    /**
                     * Merge glueBit, textureBit and aoBit into glueMask
                     */
                    BlockTypeInfo nextVoxelTexture = loadBTI(cF);

                    // накопленная маска
                    glueBitMaskP |= ((nextVoxelTexture.equals(currentVoxel) && currentAoP == nextAoP ? 2 : 0) // старший бит - текстура совпадает со следующим вокселем
                            | glueP) << shift << shift;
                    glueBitMaskM |= ((nextVoxelTexture.equals(currentVoxel) && currentAoM == nextAoM ? 2 : 0) // старший бит - текстура совпадает со следующим вокселем
                            | glueM) << shift << shift;

                    currentVoxel = nextVoxelTexture;
                    currentAoP   = nextAoP;
                    currentAoM   = nextAoM;
                }
                int cooIndex = (c.get(ITERATE_AXIS_U) << 4) + c.get(ITERATE_AXIS_V);
                int slicePIndex = (ncRight.getI() << 8) + cooIndex;
                int sliceMIndex = (ncLeft.getI() << 8) + cooIndex;
                solidFaceField[slicePIndex]   = solidBitMaskP;
                solidFaceField[sliceMIndex]   = solidBitMaskM;
                visibleFaceField[slicePIndex] = glueBitMaskP;
                visibleFaceField[sliceMIndex] = glueBitMaskM;

            }
        }
    }

    public void recalcSolidAndGlueTextureBitMaskAxis(int axis) {
        //  X => YZ, для nc=XP,XM , U=X, V=Y, биты маски вдоль Z
        /**
         */

        VoxelCoo c = new VoxelCoo();
        VoxelCoo cF = new VoxelCoo(); // one step right from c
        NeighborCode ncRight = N[axis];
        NeighborCode ncLeft = ncRight.getOpposite();
        final int ITERATE_AXIS_U = U[axis];
        final int ITERATE_AXIS_V = V[axis];
        final int ALONG_AXIS = A[axis];
        BlockTypeInfo currentVoxel;
        BlockTypeInfo nextVoxel = null;

        int solidCooIndex;
        int solidSliceRIndex;
        int solidSliceLIndex;
        int solidBitMaskR;
        int solidBitMaskL;
        int textureMaskR;
        int textureMaskL;
        int solidBitValue;
        int solidFaceR;
        int solidFaceL;
        boolean lastInRow;
        while (c.iterate(ITERATE_AXIS_U, -1, 16)) {
            int solidAxisUindex = (c.get(ITERATE_AXIS_U) + 1) * 18;
            while (c.iterate(ITERATE_AXIS_V, -1, 16)) {
                solidBitMaskR = 0;
                solidBitMaskL = 0;
                textureMaskR  = 0;
                textureMaskL  = 0;
                solidBitValue = 1 << 7;

                c.set(ALONG_AXIS, -1);
                cF.assign(c);
                currentVoxel = loadBTI(c);
                while (c.iterate(ALONG_AXIS, -1, 16)) {
                    cF.inc(ALONG_AXIS);
                    lastInRow  = c.get(ALONG_AXIS) == 16;
                    nextVoxel  = !lastInRow ? loadBTI(cF) : nextVoxel;
                    solidFaceR = currentVoxel.ifSolidFace(ncRight, solidBitValue);
                    solidBitMaskR |= solidFaceR;
                    solidFaceL = currentVoxel.ifSolidFace(ncLeft, solidBitValue);
                    solidBitMaskL |= solidFaceL;

                    /**
                     * calculate glue-texture bit (hi bit of two bit mask per voxel) for current voxel, right and left faces
                     */
                    textureMaskR |= currentVoxel == nextVoxel ? solidBitValue : 0;
                    textureMaskL |= currentVoxel == nextVoxel ? solidBitValue : 0;

                    solidBitValue <<= 1;

                    currentVoxel = nextVoxel;
                }
                solidCooIndex                                    = solidAxisUindex + (c.get(ITERATE_AXIS_V) + 1);
                solidSliceRIndex                                 = (ncRight.getI324()) + solidCooIndex;
                solidSliceLIndex                                 = (ncLeft.getI324()) + solidCooIndex;
                solidFaceField[solidSliceRIndex]                 = solidBitMaskR;
                solidFaceField[solidSliceLIndex]                 = solidBitMaskL;
                textureEqualsWithNextFaceField[solidSliceRIndex] = textureMaskR;
                textureEqualsWithNextFaceField[solidSliceLIndex] = textureMaskL;
            }
        }
    }

    public void recalcGlueFacesBitMaskAxis(int axis) {
        //  X => YZ, для nc=XP,XM , U=X, V=Y, биты маски вдоль Z
        /**
         */

        VoxelCoo c = new VoxelCoo();
        VoxelCoo cR = new VoxelCoo(); // one step right from c
        VoxelCoo cL = new VoxelCoo(); // one step left from c
        NeighborCode ncRight = N[axis];
        NeighborCode ncLeft = ncRight.getOpposite();
        final int ITERATE_AXIS_U = U[axis];
        final int ITERATE_AXIS_V = V[axis];
        int solidSliceRIndex;
        int solidSliceROIndex;
        int solidSliceLIndex;
        int solidSliceLOIndex;
        while (c.iterate(ITERATE_AXIS_U)) {
            while (c.iterate(ITERATE_AXIS_V)) {
                cR.assign(c).inc(ITERATE_AXIS_U);
                cL.assign(c).dec(ITERATE_AXIS_U);
                solidSliceRIndex  = (ncRight.getI324()) + (c.get(ITERATE_AXIS_U) + 1) * 18 + (c.get(ITERATE_AXIS_V) + 1);
                solidSliceROIndex = (ncLeft.getI324()) + (cR.get(ITERATE_AXIS_U) + 1) * 18 + (cR.get(ITERATE_AXIS_V) + 1);
                solidSliceLIndex  = (ncLeft.getI324()) + (c.get(ITERATE_AXIS_U) + 1) * 18 + (c.get(ITERATE_AXIS_V) + 1);
                solidSliceLOIndex = (ncRight.getI324()) + (cL.get(ITERATE_AXIS_U) + 1) * 18 + (cL.get(ITERATE_AXIS_V) + 1);

                visibleFaceField[solidSliceRIndex] = solidFaceField[solidSliceRIndex] & ~solidFaceField[solidSliceROIndex];
                visibleFaceField[solidSliceLIndex] = solidFaceField[solidSliceLIndex] & ~solidFaceField[solidSliceLOIndex];
//                meshingDataField[(ncRight.getI() << 8) + (c.get(ITERATE_AXIS_U) << 4) + c.get(ITERATE_AXIS_V)] |= ((int) doubleBits((solidFaceField[solidSliceRIndex] & ~solidFaceField[solidSliceROIndex]) >>> 8)) & 0x55555555;
//                meshingDataField[(ncLeft.getI() << 8) + (c.get(ITERATE_AXIS_U) << 4) + c.get(ITERATE_AXIS_V)] |= ((int) doubleBits((solidFaceField[solidSliceLIndex] & ~solidFaceField[solidSliceLOIndex]) >>> 8)) & 0x55555555;
            }
        }
    }

    public void recalcVoxelsAoFactors() {
        //  X => YZ, для nc=XP,XM , U=X, V=Y, биты маски вдоль Z
        /**
         */

        VoxelCoo c = new VoxelCoo();

        while (c.iterateX()) {
            while (c.iterateY()) {
                while (c.iterateZ()) {
                    if (loadBTI(c) == AIR_BLOCK) {
                        continue;
                    }
                    forEachNeighborCode(nc -> aoFactorFaceField[(nc.getI() << 12) + c.idx0()] = (byte) calcFaceAoFactor2(c, nc));
                }
            }
        }
    }

    private int calcFaceAoFactor(VoxelCoo c, NeighborCode ncBase) {
        AtomicInteger currentAo = new AtomicInteger();
        profile("calcFaceAoFactor", () -> {
            NeighborCode ncUp = null;
            NeighborCode ncVp = null;
            if (ncBase == XP || ncBase == XM) {
                ncUp = ZP;
                ncVp = YP;
            }
            if (ncBase == YP || ncBase == YM) {
                ncUp = XP;
                ncVp = ZP;
            }
            if (ncBase == ZP || ncBase == ZM) {
                ncUp = YP;
                ncVp = XP;
            }
            assert ncUp != null;
            NeighborCode ncUm = ncUp.getOpposite();
            NeighborCode ncVm = ncVp.getOpposite();

            VoxelCoo baseCoo = new VoxelCoo(c).step(ncBase);
            VoxelCoo tmp = new VoxelCoo();
            BlockTypeInfo btiUp = loadBTI(tmp.assign(baseCoo).step(ncUp));// loadWithNeighborTwoStep(ncBase, nc1p, c); //шаг вправо(XP), шаг вперед(ZP), взять текстуру по направлению назад(ZM)
            BlockTypeInfo btiUm = loadBTI(tmp.assign(baseCoo).step(ncUm));//loadWithNeighborTwoStep(ncBase, nc1m, c); //
            BlockTypeInfo btiVp = loadBTI(tmp.assign(baseCoo).step(ncVp));//loadWithNeighborTwoStep(ncBase, nc2p, c); //
            BlockTypeInfo btiVm = loadBTI(tmp.assign(baseCoo).step(ncVm));//loadWithNeighborTwoStep(ncBase, nc2m, c); //
            BlockTypeInfo btiUpVp = loadBTI(tmp.assign(baseCoo).step(ncUp).step(ncVp)); //loadWithNeighborThreeStep(ncBase, ncUp, ncVp, c); //
            BlockTypeInfo btiUpVm = loadBTI(tmp.assign(baseCoo).step(ncUp).step(ncVm)); //loadWithNeighborThreeStep(ncBase, ncUp, ncVm, c); //
            BlockTypeInfo btiUmVp = loadBTI(tmp.assign(baseCoo).step(ncUm).step(ncVp)); //loadWithNeighborThreeStep(ncBase, ncUm, ncVp, c); //
            BlockTypeInfo btiUmVm = loadBTI(tmp.assign(baseCoo).step(ncUm).step(ncVm)); //loadWithNeighborThreeStep(ncBase, ncUm, ncVm, c); //
            currentAo.set(aoFactors(
                    btiUm.ifSolidFace(ncUp, 4) | btiUmVm.ifSolidFace(ncUp, 2) | btiVm.ifSolidFace(ncUp, 1),
                    btiUm.ifSolidFace(ncVp, 4) | btiUmVp.ifSolidFace(ncVp, 2) | btiVp.ifSolidFace(ncVp, 1),
                    btiUp.ifSolidFace(ncVm, 4) | btiUpVm.ifSolidFace(ncVm, 2) | btiVm.ifSolidFace(ncVm, 1),
                    btiUp.ifSolidFace(ncUm, 4) | btiUpVp.ifSolidFace(ncUm, 2) | btiVp.ifSolidFace(ncUm, 1)
            ));
        });
//        return 0xFF;
        return currentAo.get();
    }


    private int calcFaceAoFactor2(VoxelCoo origCoo, NeighborCode ncBase) {
        byte currentAo = 0;
        //VoxelCoo c = new VoxelCoo();
        int aUmAm = 0;
        int aUmA0 = 0;
        int aUmAp = 0;
        int aUpAm = 0;
        int aUpA0 = 0;
        int aUpAp = 0;
        int aUm = 0;
        int aUp = 0;
        int aVm = 0;
        int aVp = 0;
        VoxelCoo c = new VoxelCoo();
        c.assign(origCoo).step(ncBase);
        if (ncBase == XP || ncBase == XM) {
            int shiftAlongV = c.get(Y_AXIS) + 7;
            int shiftAlongA = c.get(X_AXIS) + 8;

            // Получим 4ре полные солид маски вдоль прилегающих плоскостей
            aUmAm = (solidFaceField[YPi324 + (c.get(Y_AXIS) - 1 + 1) * 18 + (c.get(Z_AXIS) - 1 + 1)] >> shiftAlongA) & 1;
            aUmA0 = (solidFaceField[YPi324 + (c.get(Y_AXIS) - 1 + 1) * 18 + (c.get(Z_AXIS) + 0 + 1)] >> shiftAlongA) & 1;
            aUmAp = (solidFaceField[YPi324 + (c.get(Y_AXIS) - 1 + 1) * 18 + (c.get(Z_AXIS) + 1 + 1)] >> shiftAlongA) & 1;
            aUpAm = (solidFaceField[YMi324 + (c.get(Y_AXIS) + 1 + 1) * 18 + (c.get(Z_AXIS) - 1 + 1)] >> shiftAlongA) & 1;
            aUpA0 = (solidFaceField[YMi324 + (c.get(Y_AXIS) + 1 + 1) * 18 + (c.get(Z_AXIS) + 0 + 1)] >> shiftAlongA) & 1;
            aUpAp = (solidFaceField[YMi324 + (c.get(Y_AXIS) + 1 + 1) * 18 + (c.get(Z_AXIS) + 1 + 1)] >> shiftAlongA) & 1;
            aVm   = (solidFaceField[ZPi324 + (c.get(Z_AXIS) - 1 + 1) * 18 + (c.get(X_AXIS) + 1)] >> shiftAlongV) & 7;
            aVp   = (solidFaceField[ZMi324 + (c.get(Z_AXIS) + 1 + 1) * 18 + (c.get(X_AXIS) + 1)] >> shiftAlongV) & 7;
// теперь нужно получить из них 3 бита для каждого прилегающего угла
            // маска для блока в координате 0 выглядит так 0000.0000:0000.0000:0000.0011:1000.0000
            //                                             0000.0000:0000.0000:0000.0000:0000.0111 <- нужно сдвинуть на 7 + координата блока
            //                                             но можно сделать наоборот, солид маску сдвинуть вправо на столько же и & 7
            aUm = aUmAm | aUmA0 << 1 | aUmAp << 2;
            aUp = aUpAm | aUpA0 << 1 | aUpAp << 2;
            // теперь младшие 3 бита это солид-маска прилегающих блоков по прямой, три блока с каждой из 4х сторон.
        }
        if (ncBase == YP || ncBase == YM) {
            int shiftAlongV = c.get(Z_AXIS) + 7;
            int shiftAlongA = c.get(Y_AXIS) + 8;

            // Получим 4ре полные солид маски вдоль прилегающих плоскостей
            aUmAm = (solidFaceField[ZPi324 + (c.get(Z_AXIS) - 1 + 1) * 18 + (c.get(X_AXIS) - 1 + 1)] >> shiftAlongA) & 1;
            aUmA0 = (solidFaceField[ZPi324 + (c.get(Z_AXIS) - 1 + 1) * 18 + (c.get(X_AXIS) + 0 + 1)] >> shiftAlongA) & 1;
            aUmAp = (solidFaceField[ZPi324 + (c.get(Z_AXIS) - 1 + 1) * 18 + (c.get(X_AXIS) + 1 + 1)] >> shiftAlongA) & 1;
            aUpAm = (solidFaceField[ZMi324 + (c.get(Z_AXIS) + 1 + 1) * 18 + (c.get(X_AXIS) - 1 + 1)] >> shiftAlongA) & 1;
            aUpA0 = (solidFaceField[ZMi324 + (c.get(Z_AXIS) + 1 + 1) * 18 + (c.get(X_AXIS) + 0 + 1)] >> shiftAlongA) & 1;
            aUpAp = (solidFaceField[ZMi324 + (c.get(Z_AXIS) + 1 + 1) * 18 + (c.get(X_AXIS) + 1 + 1)] >> shiftAlongA) & 1;
            aVm   = (solidFaceField[XPi324 + (c.get(X_AXIS) - 1 + 1) * 18 + (c.get(Y_AXIS) + 1)] >> shiftAlongV) & 7;
            aVp   = (solidFaceField[XMi324 + (c.get(X_AXIS) + 1 + 1) * 18 + (c.get(Y_AXIS) + 1)] >> shiftAlongV) & 7;
// теперь нужно получить из них 3 бита для каждого прилегающего угла
            // маска для блока в координате 0 выглядит так 0000.0000:0000.0000:0000.0011:1000.0000
            //                                             0000.0000:0000.0000:0000.0000:0000.0111 <- нужно сдвинуть на 7 + координата блока
            //                                             но можно сделать наоборот, солид маску сдвинуть вправо на столько же и & 7
            aUm = aUmAm | aUmA0 << 1 | aUmAp << 2;
            aUp = aUpAm | aUpA0 << 1 | aUpAp << 2;
            // теперь младшие 3 бита это солид-маска прилегающих блоков по прямой, три блока с каждой из 4х сторон.
        }
        if (ncBase == ZP || ncBase == ZM) {
            int shiftAlongV = c.get(X_AXIS) + 7;
            int shiftAlongA = c.get(Z_AXIS) + 8;

            // Получим 4ре полные солид маски вдоль прилегающих плоскостей
            aUmAm = (solidFaceField[XPi324 + (c.get(X_AXIS) - 1 + 1) * 18 + (c.get(Y_AXIS) - 1 + 1)] >> shiftAlongA) & 1;
            aUmA0 = (solidFaceField[XPi324 + (c.get(X_AXIS) - 1 + 1) * 18 + (c.get(Y_AXIS) + 0 + 1)] >> shiftAlongA) & 1;
            aUmAp = (solidFaceField[XPi324 + (c.get(X_AXIS) - 1 + 1) * 18 + (c.get(Y_AXIS) + 1 + 1)] >> shiftAlongA) & 1;
            aUpAm = (solidFaceField[XMi324 + (c.get(X_AXIS) + 1 + 1) * 18 + (c.get(Y_AXIS) - 1 + 1)] >> shiftAlongA) & 1;
            aUpA0 = (solidFaceField[XMi324 + (c.get(X_AXIS) + 1 + 1) * 18 + (c.get(Y_AXIS) + 0 + 1)] >> shiftAlongA) & 1;
            aUpAp = (solidFaceField[XMi324 + (c.get(X_AXIS) + 1 + 1) * 18 + (c.get(Y_AXIS) + 1 + 1)] >> shiftAlongA) & 1;
            aVm   = (solidFaceField[YPi324 + (c.get(Y_AXIS) - 1 + 1) * 18 + (c.get(Z_AXIS) + 1)] >> shiftAlongV) & 7;
            aVp   = (solidFaceField[YMi324 + (c.get(Y_AXIS) + 1 + 1) * 18 + (c.get(Z_AXIS) + 1)] >> shiftAlongV) & 7;
// теперь нужно получить из них 3 бита для каждого прилегающего угла
            // маска для блока в координате 0 выглядит так 0000.0000:0000.0000:0000.0011:1000.0000
            //                                             0000.0000:0000.0000:0000.0000:0000.0111 <- нужно сдвинуть на 7 + координата блока
            //                                             но можно сделать наоборот, солид маску сдвинуть вправо на столько же и & 7
            aUm = aUmAm | aUmA0 << 1 | aUmAp << 2;
            aUp = aUpAm | aUpA0 << 1 | aUpAp << 2;
            // теперь младшие 3 бита это солид-маска прилегающих блоков по прямой, три блока с каждой из 4х сторон.
        }

        int indexMask = aUm | aUp << 3 | aVm << 6 | aVp << 9;
        currentAo = AO_FACTOR_MATRIX[indexMask];


        return currentAo;
    }

    public void enableLoadingMode() {
        this.isLoadingMode = true;
    }

    public void disableLoadingMode() {
        this.isLoadingMode = false;
    }

    /**
     * Encode the four ambient occlusion factors into a single byte.
     */
    private int aoFactors(int n00, int n10, int n01, int n11) {
        return (aoFactor(n00) | aoFactor(n10) << 2 | aoFactor(n01) << 4 | aoFactor(n11) << 6);
    }

    /**
     * Compute the ambient occlusion factor from a vertex's neighbor configuration <code>n</code>.
     * Используются только три младших бита, то есть 8 вариантов на вход
     * Результат
     * 000 - 11
     * 001 - 10
     * 010 - 10
     * 011 - 01
     * 100 - 10
     * 101 - 00
     * 110 - 01
     * 111 - 00
     * <p>
     * А Если посчитать результат для формата 4 бит? 2 бита по одной стороне и 2 бита по другой, без промежуточной склейки в 3 битовый вариант
     * 0000  -> 0  =>	ao = 3
     * 0001  -> 1  =>	ao = 2
     * 0010  -> 2  =>	ao = 2
     * 0011  -> 3  =>	ao = 1
     * 0100  -> 2  =>	ao = 2
     * 0101  -> 3  =>	ao = 1
     * 0110  -> 2  =>	ao = 2
     * 0111  -> 3  =>	ao = 1
     * 1000  -> 4  =>	ao = 2
     * 1001  -> 5  =>	ao = 0
     * 1010  -> 6  =>	ao = 1
     * 1011  -> 7  =>	ao = 0
     * 1100  -> 6  =>	ao = 1
     * 1101  -> 7  =>	ao = 0
     * 1110  -> 6  =>	ao = 1
     * 1111  -> 7  =>	ao = 0
     * <p>
     * * 0000   => 3
     * * 0001   => 2
     * * 0010   => 2
     * * 0011   => 1
     * * 0100   => 2
     * * 0101   => 1
     * * 0110   => 2
     * * 0111   => 1
     * * 1000   => 2
     * * 1001   => 0
     * * 1010   => 1
     * * 1011   => 0
     * * 1100   => 1
     * * 1101   => 0
     * * 1110   => 1
     * * 1111   => 0
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     */
    private int aoFactor(int n) {
        return (n & 1) == 1 && (n & 4) == 4 ? 0 : 3 - Integer.bitCount(n);
    }


    public void calcAirFlowThroughChunk() {
        /**
         * Optimization : set constant value for empty block
         */
//        if (isEmpty) {
//            airFlowThroughChunk.setToTrue();
//            return;
//        }


        /**
         * Подготовка массива для ChunkVisibility
         * младшие три бита каждого значения означают отсутствие/наличие solid face по каждой из осей
         * 0000.0ZYX
         * 0 - блок вдоль оси просматривается
         * 1 - блок вдоль оси не просматривается
         */
        ChunkVisibility chunkVisibility = new ChunkVisibility();
        int[][][] grid = new int[16][16][16];
        // Для среза в плоскостях Y и X итерируем по Z, для среза в плоскости Z итерируем по Y
        //  X => YZ, для nc=XP,XM , U=X, V=Y, биты маски вдоль Z
        //  Y => XZ, для nc=YP,YM , U=Y, V=X, биты маски вдоль Z
        //  Z => XY, для nc=ZP,ZM , U=Z, V=X, биты маски вдоль Y
        VoxelCooIteratorXAxis iter = new VoxelCooIteratorXAxis();
        VoxelCoo neighborCoo = new VoxelCoo();
        while (iter.iterateU()) {
            while (iter.iterateV()) {
                while (iter.iterateA()) {
                    int x = iter.getX();
                    int y = iter.getY();
                    int z = iter.getZ();
                    grid[x][y][z] = loadBTI(iter.getC()).getSolidMask();
                    NeighborCode nc = iter.getC().getNeighborEdge();
                    if (nc != null) {
                        grid[x][y][z] |= loadBTI(neighborCoo.assign(iter.getC()).step(nc)).ifSolidFace(nc.getOpposite(), nc.getB());
//                        grid[x][y][z] |= loadBTI(neighborCoo.assign(iter.getC()).step(nc)).getSolidMask();
                    }
                }
            }
        }
//        for (int sliceU = 0; sliceU < 32; sliceU++) {
//            for (int sliceV = 0; sliceV < 32; sliceV++) {
//                NeighborCode op = XP;
//                int fullMaskX = getFullSolidMaskForSliceUV(XP, sliceU, sliceV) | getFullSolidMaskForSliceUV(XM, sliceU, sliceV);
//                int fullMaskY = getFullSolidMaskForSliceUV(YP, sliceU, sliceV) | getFullSolidMaskForSliceUV(YM, sliceU, sliceV);
//                int fullMaskZ = getFullSolidMaskForSliceUV(ZP, sliceU, sliceV) | getFullSolidMaskForSliceUV(ZM, sliceU, sliceV);
//                for (int i = 0; i < 32; i++) {
//                    grid[sliceU][sliceV][i] |= (fullMaskX & 0x1) * XP.getCvd();
//                    fullMaskX = fullMaskX >>> 1;
//                    grid[sliceV][sliceU][i] |= (fullMaskY & 0x1) * YP.getCvd();
//                    fullMaskY = fullMaskY >>> 1;
//                    grid[sliceV][i][sliceU] |= (fullMaskZ & 0x1) * ZP.getCvd();
//                    fullMaskZ = fullMaskZ >>> 1;
//                }
//            }

        airFlowThroughChunk.assign(chunkVisibility.calculateFull(grid));
//        System.out.println(airFlowThroughChunk);
    }
//
//    private int getFullSolidMaskForSliceUV(NeighborCode op, int sliceU, int sliceV) {
//        int currentFaceSideIndex = op.getI();
//        int oppositeFaceSideIndex = op.getOpposite().getI();
//        int deltaIndexForOppositeRow = op.getD();
//        int currentSolidFacesRowFieldIndex = (currentFaceSideIndex << 8) + (sliceU << 4) + sliceV;
//        int oppositeSolidFacesRowFieldIndex = (oppositeFaceSideIndex << 8) + ((sliceU + deltaIndexForOppositeRow) << 4) + sliceV;
//        int solidFacesRowMaskForCurrentSlice = solidFaceField[currentSolidFacesRowFieldIndex];
//        boolean isOnEdge = (deltaIndexForOppositeRow > 0 && sliceU == MAX_VOXEL_COO) || (deltaIndexForOppositeRow < 0 && sliceU == 0);
//        int oppositeSliceU = (sliceU + deltaIndexForOppositeRow) & MAX_VOXEL_COO;
//        int solidFacesRowMaskForOppositeSlice = !isOnEdge ? solidFaceField[oppositeSolidFacesRowFieldIndex] : getOppositeSolidFacesRowMask(oppositeSliceU, sliceV, op);
//
//        int fullMask = solidFacesRowMaskForCurrentSlice | solidFacesRowMaskForOppositeSlice;
//        return fullMask;
//    }
}



