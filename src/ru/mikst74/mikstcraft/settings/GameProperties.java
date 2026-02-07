package ru.mikst74.mikstcraft.settings;

import org.lwjgl.system.Configuration;

import java.text.NumberFormat;

/**
 * Created by Mikhail Krinitsyn on 09.01.2026
 */
public class GameProperties {
    public static final boolean DEBUG = has("debug", false);
    public static final boolean DEBUG2 = has("debug2", false);
    public static final boolean GLDEBUG = has("gldebug", true);
    public static final boolean FULLSCREEN = has("fullscreen", false);
    public static final boolean VSYNC = has("vsync", true);
    public static final boolean GRAB_CURSOR = has("grabCursor", true);
    public static final NumberFormat INT_FORMATTER = NumberFormat.getIntegerInstance();
    public static final NumberFormat PERCENT_FORMATTER = NumberFormat.getPercentInstance();
    /**
     * The type/value of the empty voxel.
     */
    public static final byte EMPTY_VOXEL = 0;
    /**
     * The width and depth of a chunk (in number of voxels).
     */
    public static final int CHUNK_SIZE_SHIFT = 4; // 1: 2x2x2,  4: 16x16x16, 5: 32x32x32 ! 5 it is max size for chunk
    public static final int CHUNK_SIZE = 1 << CHUNK_SIZE_SHIFT;
    public static final int CHUNK_SIZE_M1 = CHUNK_SIZE - 1;
    /**
     * The height of a chunk (in number of voxels).
     */
    public static final int CHUNK_HEIGHT = 8;
    /**
     * The index/token used in an index buffer for primitive restart.
     */
    public static final int PRIMITIVE_RESTART_INDEX = 0xFFFF;
    /**
     * The initial capacity of per-face data buffers. The unit is in number of faces, not bytes.
     */
    public static final int INITIAL_PER_FACE_BUFFER_CAPACITY = 1 << 25;
    /**
     * The maximum allowed number of active rendered chunks. We use this as an upper limit when
     * allocating (multi-buffered) arrays/buffer objects.
     */
    public static final int MAX_ACTIVE_CHUNKS = 1 << 16;
    /**
     * The minimum height for the generated terrain.
     */
    public static final int BASE_Y = 50;
    /**
     * The chunk offset for the noise function.
     */
    public static final int GLOBAL_X = 2500;
    public static final int GLOBAL_Z = 851;
    /**
     * The total height of the player's collision box.
     */
    public static final float PLAYER_HEIGHT = 2.1f;//1.80f;
    /**
     * The eye level of the player.
     */
    public static final float PLAYER_EYE_HEIGHT = 1.70f;
    /**
     * The width of the player's collision box.
     */
    public static final float PLAYER_WIDTH = 1.1f;//0.4f;
    /**
     * The number of chunks whose voxel fields are kept in memory to load or store voxels.
     */
    public static final int MAX_CACHED_CHUNKS = 6;
    /**
     * The number of chunks, starting from the player's position, that should be visible in any given
     * direction.
     */
    public static  int MAX_RENDER_DISTANCE_CHUNKS = 3;//40;
    /**
     * Distance to the far plane.
     */
    public static   float FAR = MAX_RENDER_DISTANCE_CHUNKS * CHUNK_SIZE *4f;//2.0f;
    /**
     * The maximum render distance in meters.
     */
    public static final int MAX_RENDER_DISTANCE_METERS = (MAX_RENDER_DISTANCE_CHUNKS << CHUNK_SIZE_SHIFT);
    public static  int MAX_RENDER_DISTANCE_METERS2 = MAX_RENDER_DISTANCE_METERS * MAX_RENDER_DISTANCE_METERS;
    /**
     * The vertical field of view of the camera in degrees.
     */
    public static  int FOV_DEGREES =   72;// 72;//120;//72;
    /**
     * The maximum number of async. chunk creation tasks submitted for execution.
     * <p>
     * When this number of tasks is reached,
     * {@link #createInRenderDistanceAndDestroyOutOfRenderDistanceChunks()} will not create any new
     * chunk creation tasks until the number decreases.
     */
    public static final int MAX_NUMBER_OF_CHUNK_TASKS = 16;
    /**
     * Number of distinct buffer regions for dynamic buffers that update every frame, in order to avoid
     * GPU/CPU stalls when waiting for a buffer region to become ready for update after a sourcing draw
     * command finished with it.
     */
    public static final int DYNAMIC_BUFFER_OBJECT_REGIONS = 4;
    /**
     * The number of coverage samples for a multisampled framebuffer, iff
     * {@link #useNvMultisampleCoverage} is <code>true</code>.
     */
    public static final int COVERAGE_SAMPLES = 2;
    /**
     * The number of color samples for a multisampled framebuffer, iff {@link #useNvMultisampleCoverage}
     * is <code>true</code>.
     */
    public static final int COLOR_SAMPLES = 1;
    /**
     * Distance to the near plane.
     */
    public static final float NEAR = 0.1f;
    /**
     * Ambient occlusion factors to be included as a #define in shaders.
     */
//    public static final String AO_FACTORS = "0.10, 0.20, 0.35,1.0";
    public static final String AO_FACTORS = "0.60, 0.70, 0.85, 1.0";

    static {
        if (GameProperties.DEBUG) {
            // When we are in debug mode, enable all LWJGL debug flags
            Configuration.DEBUG.set(true);
            Configuration.DEBUG_FUNCTIONS.set(true);
            Configuration.DEBUG_LOADER.set(true);
            Configuration.DEBUG_MEMORY_ALLOCATOR.set(true);
            Configuration.DEBUG_MEMORY_ALLOCATOR_FAST.set(true);
            Configuration.DEBUG_STACK.set(true);
        } else {
            Configuration.DISABLE_CHECKS.set(true);
        }
        /* Configure LWJGL MemoryStack to 1024KB */
        Configuration.STACK_SIZE.set(1024);
    }

    public static boolean has(String prop, boolean def) {
        String value = System.getProperty(prop);
        return value != null ? value.isEmpty() || Boolean.parseBoolean(value) : def;
    }
}
