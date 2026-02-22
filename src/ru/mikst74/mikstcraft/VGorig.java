///*
// * Copyright LWJGL. All rights reserved.
// * License terms: https://www.lwjgl.org/license
// */
//package org.lwjgl.demo.mikstcraft;
//
//import org.joml.*;
//import org.lwjgl.PointerBuffer;
//import org.lwjgl.demo.mikstcraft.model.Chunk;
//import org.lwjgl.demo.mikstcraft.model.Contact;
//import org.lwjgl.demo.mikstcraft.model.Material;
//import org.lwjgl.demo.mikstcraft.model.Person;
//import org.lwjgl.demo.mikstcraft.model.font.Font;
//import org.lwjgl.demo.mikstcraft.render.RenderedWorld;
//import org.lwjgl.demo.mikstcraft.render.buffers.PerFaceBuffers;
//import org.lwjgl.demo.mikstcraft.render.model.FrustumPlanes;
//import org.lwjgl.demo.mikstcraft.render.text.FontRender;
//import org.lwjgl.demo.mikstcraft.render.text.RenderText;
//import org.lwjgl.demo.mikstcraft.render.virtual.VirtualPlaneRender;
//import org.lwjgl.demo.mikstcraft.storage.WorldSaver;
//import org.lwjgl.demo.mikstcraft.texture.TextureLoader;
//import org.lwjgl.demo.mikstcraft.util.DelayedRunnable;
//import org.lwjgl.demo.mikstcraft.world.WorldMap;
//import org.lwjgl.demo.mikstcraft.world.chunk.ChunkDistance;
//import org.lwjgl.glfw.GLFW;
//import org.lwjgl.glfw.GLFWVidMode;
//import org.lwjgl.opengl.GL;
//import org.lwjgl.opengl.GLCapabilities;
//import org.lwjgl.system.Callback;
//import org.lwjgl.system.MemoryStack;
//
//import java.io.IOException;
//import java.nio.IntBuffer;
//import java.text.NumberFormat;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//
//import static java.lang.Float.NEGATIVE_INFINITY;
//import static java.lang.Float.POSITIVE_INFINITY;
//import static java.lang.Math.*;
//import static java.util.Collections.sort;
//import static org.lwjgl.demo.mikstcraft.GameProperties.DEBUG2;
//import static org.lwjgl.demo.mikstcraft.render.opengl.ShaderCreator.createShader;
//import static org.lwjgl.demo.mikstcraft.util.BackgroundExecutor.executorService;
//import static org.lwjgl.demo.mikstcraft.util.BackgroundExecutor.updateAndRenderRunnables;
//import static org.lwjgl.demo.mikstcraft.util.OpenGLProperties.*;
//import static org.lwjgl.demo.mikstcraft.util.Profiler.printProfile;
//import static org.lwjgl.demo.mikstcraft.util.Profiler.profile;
//import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
//import static org.lwjgl.glfw.GLFW.*;
//import static org.lwjgl.opengl.ARBBufferStorage.*;
//import static org.lwjgl.opengl.ARBClearBufferObject.glClearBufferSubData;
//import static org.lwjgl.opengl.ARBClipControl.GL_ZERO_TO_ONE;
//import static org.lwjgl.opengl.ARBClipControl.glClipControl;
//import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_OUTPUT_SYNCHRONOUS_ARB;
//import static org.lwjgl.opengl.ARBDrawIndirect.GL_DRAW_INDIRECT_BUFFER;
//import static org.lwjgl.opengl.ARBIndirectParameters.GL_PARAMETER_BUFFER_ARB;
//import static org.lwjgl.opengl.ARBIndirectParameters.glMultiDrawElementsIndirectCountARB;
//import static org.lwjgl.opengl.ARBMultiDrawIndirect.glMultiDrawElementsIndirect;
//import static org.lwjgl.opengl.ARBShaderAtomicCounters.GL_ATOMIC_COUNTER_BUFFER;
//import static org.lwjgl.opengl.ARBShaderImageLoadStore.GL_COMMAND_BARRIER_BIT;
//import static org.lwjgl.opengl.ARBShaderImageLoadStore.glMemoryBarrier;
//import static org.lwjgl.opengl.ARBShaderStorageBufferObject.GL_SHADER_STORAGE_BUFFER;
//import static org.lwjgl.opengl.GL11.glBindTexture;
//import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
//import static org.lwjgl.opengl.GL13.glActiveTexture;
//import static org.lwjgl.opengl.GL33C.*;
//import static org.lwjgl.opengl.GLUtil.setupDebugMessageCallback;
//import static org.lwjgl.opengl.NVFramebufferMultisampleCoverage.glRenderbufferStorageMultisampleCoverageNV;
//import static org.lwjgl.opengl.NVRepresentativeFragmentTest.GL_REPRESENTATIVE_FRAGMENT_TEST_NV;
//import static org.lwjgl.system.MemoryStack.stackPush;
//import static org.lwjgl.system.MemoryUtil.*;
//
///**
// * A simple voxel game.
// *
// * @author Kai Burjack
// */
//public class VoxelGame2GL {
//
//    private final WorldMap worldMap = new WorldMap();
//    private final Person player = new Person();
//    private final RenderedWorld renderedWorld = new RenderedWorld(worldMap, player.getPosition());
//    private final WorldSaver saver = new WorldSaver(worldMap);
//    /*
//     * Comparators to sort the chunks by whether they are in the view frustum and by distance to the
//     * player.
//     */
//
//
//    /**
//     * Tasks that must be run on the update/render thread, usually because they involve calling OpenGL
//     * functions.
//     */
//
//    private long window;
//    private int width;
//    private int height;
//    //    private final Vector3f playerAcceleration = new Vector3f(0, -30.0f, 0);
////    private final Vector3f playerVelocity = new Vector3f();
////    private final Vector3d playerPosition = new Vector3d(0, 240, 0);
//    private float angx, angy, dangx, dangy, angz;
//    private final Matrix4f pMat = new Matrix4f();
//    private final Matrix4x3f vMat = new Matrix4x3f();
//    private final Matrix4f mvpMat = new Matrix4f();
//    private final Matrix4f imvpMat = new Matrix4f();
//    private final Matrix4f tmpMat = new Matrix4f();
//    private final Quaternionf tmpq = new Quaternionf();
//    private Material[] materials = new Material[512];
//    private Callback debugProc;
//    private GLCapabilities caps;
//
//
//    /* Other queried OpenGL state/configuration */
//    private int uniformBufferOffsetAlignment;
//
//    int grassTextureId;
//
//    /**
//     * Index identifying the current region of any dynamic buffer which we will use for updating and
//     * drawing from.
//     */
//    public static int currentDynamicBufferIndex;
//    /**
//     * Fence sync objects for updating/drawing a particular region of the indirect draw buffer.
//     */
//    private final long[] dynamicBufferUpdateFences = new long[GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS];
//    private int numChunksInFrustum;
//    private int fbo, colorRbo, depthRbo;
//
//    private final boolean[] keydown = new boolean[GLFW_KEY_LAST + 1];
//    private int mouseX, mouseY;
//    private final Vector3f tmpv3f = new Vector3f();
//    private final Vector4f tmpv4f = new Vector4f();
//    private boolean jumping;
//
//
//    /* Resources for drawing the chunks */
////    private int chunkInfoBufferObject;
//    private int chunksProgram;
//    private int materialsTexture;
//    private int chunksProgramUboBlockIndex;
//    private int chunksProgramUbo;
//    private long chunksProgramUboAddr;
//    private static final int chunksProgramUboSize = 4 * (16 + 2 * 4);
//
//    /* Resources for drawing chunks' bounding boxes to fill visibility buffer */
//    private int visibilityFlagsBuffer;
//    private int boundingBoxesVao;
//    private int boundingBoxesProgram;
//    private int boundingBoxesProgramUboBlockIndex;
//    private int boundingBoxesProgramUbo;
//    private long boundingBoxesProgramUboAddr;
//    private int boundingBoxesVertexBufferObject;
//    private long boundingBoxesVertexBufferObjectAddr;
//    private static final int boundingBoxesProgramUboSize = 4 * (16 + 4);
//
//    /* GUI */
//    private int guiVao;
//    private int guiProgram;
//    private int guiVertexBufferObject;
//    private int guiProgramUbo;
//    private int guiProgramUboBlockIndex;
//
//
//    /* */
//
//    /* Resources for collecting draw calls using the visibility buffer */
//    private int collectDrawCallsProgram;
//    private int atomicCounterBuffer;
//    private int indirectDrawCulledBuffer;
//
//    /* Resources for drawing the "selection" quad */
//    public static int nullVao;
//    private int selectionProgram;
//    private int selectionProgramUboBlockIndex;
//    private int selectionProgramUbo;
//    private long selectionProgramUboAddr;
//    private static final int selectionProgramUboSize = 4 * 16;
//    private final Vector3i selectedVoxelPosition = new Vector3i();
//    private final Vector3i sideOffset = new Vector3i();
//    private boolean hasSelection;
//    private boolean firstCursorPos = true;
//    private boolean fly;
//    private boolean wireframe;
//    private boolean debugBoundingBoxes;
//    private int selectedMaterial = 3;
//    private Vector4f selectedMaterialAsColor = new Vector4f();
//    private FontRender font;
//    private VirtualPlaneRender virtualPlaneRender;
//
//    /**
//     * Callback for mouse movement.
//     *
//     * @param window the window (we only have one, currently)
//     * @param x      the x coordinate
//     * @param y      the y coordinate
//     */
//    private void onCursorPos(long window, double x, double y) {
//        if (!firstCursorPos) {
//            float deltaX = (float) x - mouseX;
//            float deltaY = (float) y - mouseY;
//            dangx += deltaY;
//            dangy += deltaX;
//        }
//        firstCursorPos = false;
//        mouseX = (int) x;
//        mouseY = (int) y;
//    }
//
//    /**
//     * Calls {@link #findAndSetSelectedVoxel(float, float, float, float, float, float)} with the current
//     * player position and orientation.
//     */
//    private void determineSelectedVoxel() {
//        if (fly) {
//            hasSelection = false;
//            return;
//        }
//        Vector3f dir = tmpq.rotationX(angx).rotateY(angy).positiveZ(tmpv3f).negate();
//        findAndSetSelectedVoxel((float) player.getPosition().x, (float) player.getPosition().y, (float) player.getPosition().z, dir.x, dir.y, dir.z);
//    }
//
//    /**
//     * GLFW callback when a key is pressed/released.
//     */
//    private void onKey(long window, int key, int scancode, int action, int mods) {
//        if (key == GLFW_KEY_ESCAPE) {
//            saver.save();
//            printProfile();
//            glfwSetWindowShouldClose(window, true);
//        } else if (key >= 0) {
//            keydown[key] = action == GLFW_PRESS || action == GLFW_REPEAT;
//        }
//        handleSpecialKeys(key, action);
//    }
//
//    /**
//     * Handle special keyboard keys before storing a key press/release state in the {@link #keydown}
//     * array.
//     */
//    private void handleSpecialKeys(int key, int action) {
//        if (key == GLFW_KEY_F && action == GLFW_PRESS) {
//            fly = !fly;
//        } else if (key == GLFW_KEY_L && action == GLFW_PRESS) {
//            wireframe = !wireframe;
//        } else if (key == GLFW_KEY_K && action == GLFW_PRESS) {
//            debugBoundingBoxes = !debugBoundingBoxes;
//        }
//        if (key == GLFW_KEY_Z && action == GLFW_PRESS) {
//            System.out.println(selectedVoxelPosition.toString(NumberFormat.getIntegerInstance()));
//        }
//        if (key == GLFW_KEY_P && action == GLFW_PRESS) {
//            saver.save();
//        }
//        if (key >= GLFW_KEY_1 && key <= GLFW_KEY_9 && action == GLFW_PRESS) {
//            setSelectedMaterial((byte) ((byte) key - 48));
//        }
//        if (key == GLFW_KEY_0 && action == GLFW_PRESS) {
//            setSelectedMaterial((byte) 10);
//        }
//        if (key == GLFW_KEY_Z && action == GLFW_PRESS) {
//            GameProperties.FOV_DEGREES = 12;
//        }
//        if (key == GLFW_KEY_Z && action == GLFW_RELEASE) {
//            GameProperties.FOV_DEGREES = 72;
//        }
//
//    }
//
//
//    private void setSelectedMaterial(int m) {
//        selectedMaterial = m;
//        int colorInt = materials[selectedMaterial].col;
//        int b = (int) ((colorInt >> 16) & 0xFF);
//        int g = (int) ((colorInt >> 8) & 0xFF);
//        int r = (int) (colorInt & 0xFF);
//        selectedMaterialAsColor = (Vector4f) new Vector4f((float) r / 256, (float) g / 256, (float) b / 256, 0.75f);//. new Vector4f(1.0f, 0.3f, 0f, 1.f);
//    }
//
//    /**
//     * GLFW framebuffer size callback.
//     */
//    private void onFramebufferSize(long window, int w, int h) {
//        if (w <= 0 && h <= 0) {
//            return;
//        }
//        updateAndRenderRunnables.add(new DelayedRunnable(() -> {
//            width = w;
//            height = h;
//            createFramebufferObject();
//            glViewport(0, 0, width, height);
//            return null;
//        }, "Framebuffer size change", 0));
//    }
//
//    /**
//     * Return the voxel field value at the global position <code>(x, y, z)</code>.
//     */
//    private byte load(int wx, int wy, int wz) {
//        int cx = wx >> GameProperties.CHUNK_SIZE_SHIFT, cz = wz >> GameProperties.CHUNK_SIZE_SHIFT;
//        int x = wx - (cx << GameProperties.CHUNK_SIZE_SHIFT), z = wz - (cz << GameProperties.CHUNK_SIZE_SHIFT);
//        return worldMap.getChunkHolder().getChunkByCoordinate(cx, cz).getVoxel(x, wy, z);
//    }
//
//    /**
//     * GLSL's step function.
//     */
//    private static int step(float edge, float x) {
//        return x < edge ? 0 : 1;
//    }
//
//    /**
//     * Determine the voxel pointed to by a ray <code>(ox, oy, oz) + t * (dx, dy, dz)</code> and store
//     * the position and side offset of that voxel (if any) into {@link #selectedVoxelPosition} and
//     * {@link #sideOffset}, respectively.
//     *
//     * @param ox the ray origin's x coordinate
//     * @param oy the ray origin's y coordinate
//     * @param oz the ray origin's z coordinate
//     * @param dx the ray direction's x coordinate
//     * @param dy the ray direction's y coordinate
//     * @param dz the ray direction's z coordinate
//     */
//    private void findAndSetSelectedVoxel(float ox, float oy, float oz, float dx, float dy, float dz) {
//        /* "A Fast Voxel Traversal Algorithm for Ray Tracing" by John Amanatides, Andrew Woo */
//        float big = 1E30f;
//        int px = (int) floor(ox), py = (int) floor(oy), pz = (int) floor(oz);
//        float dxi = 1f / dx, dyi = 1f / dy, dzi = 1f / dz;
//        int sx = dx > 0 ? 1 : -1, sy = dy > 0 ? 1 : -1, sz = dz > 0 ? 1 : -1;
//        float dtx = min(dxi * sx, big), dty = min(dyi * sy, big), dtz = min(dzi * sz, big);
//        float tx = abs((px + max(sx, 0) - ox) * dxi), ty = abs((py + max(sy, 0) - oy) * dyi), tz = abs((pz + max(sz, 0) - oz) * dzi);
//        int maxSteps = 16;
//        int cmpx = 0, cmpy = 0, cmpz = 0;
//        for (int i = 0; i < maxSteps && py >= 0; i++) {
//            if (i > 0 && py < GameProperties.CHUNK_HEIGHT) {
//                if (load(px, py, pz) != 0) {
//                    selectedVoxelPosition.set(px, py, pz);
//                    sideOffset.set(-cmpx * sx, -cmpy * sy, -cmpz * sz);
//                    hasSelection = true;
//                    return;
//                }
//            }
//            /* Advance to next voxel */
//            cmpx = step(tx, tz) * step(tx, ty);
//            cmpy = step(ty, tx) * step(ty, tz);
//            cmpz = step(tz, ty) * step(tz, tx);
//            tx += dtx * cmpx;
//            ty += dty * cmpy;
//            tz += dtz * cmpz;
//            px += sx * cmpx;
//            py += sy * cmpy;
//            pz += sz * cmpz;
//        }
//        hasSelection = false;
//    }
//
//    /**
//     * GLFW callback for mouse buttons.
//     */
//    private void onMouseButton(long window, int button, int action, int mods) {
//        updateAndRenderRunnables.add(new DelayedRunnable(() -> {
//            if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS && hasSelection) {
//                placeAtSelectedVoxel();
//            } else if (button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS && hasSelection) {
//                removeSelectedVoxel();
//            }
//            return null;
//        }, "Mouse button event", 0));
//    }
//
//    private void removeSelectedVoxel() {
//        worldMap.store(selectedVoxelPosition.x, selectedVoxelPosition.y, selectedVoxelPosition.z, (byte) 0);
////        worldMap.store(selectedVoxelPosition.x, selectedVoxelPosition.y - 1, selectedVoxelPosition.z, (byte) 0);
//    }
//
//    private void placeAtSelectedVoxel() {
//        if (selectedVoxelPosition.y + sideOffset.y < 0 || selectedVoxelPosition.y + sideOffset.y >= GameProperties.CHUNK_HEIGHT) {
//            return;
//        }
//        worldMap.store(selectedVoxelPosition.x + sideOffset.x, selectedVoxelPosition.y + sideOffset.y, selectedVoxelPosition.z + sideOffset.z, (byte) selectedMaterial);
//    }
//
//    /**
//     * Register all necessary GLFW callbacks.
//     */
//    private void registerWindowCallbacks() {
//        glfwSetFramebufferSizeCallback(window, this::onFramebufferSize);
//        glfwSetKeyCallback(window, this::onKey);
//        glfwSetCursorPosCallback(window, this::onCursorPos);
//        glfwSetMouseButtonCallback(window, this::onMouseButton);
//        glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
//            if (yoffset < 0) {
//                selectedMaterial++;
//            } else {
//                selectedMaterial--;
//            }
//            if (selectedMaterial < 1) {
//                selectedMaterial = materials.length - 1;
//            } else if (selectedMaterial >= materials.length) {
//                selectedMaterial = 1;
//            }
//            setSelectedMaterial(selectedMaterial);
//        });
//    }
//
//    private void createWindow() {
//        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
//        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
////        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
////        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
//        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
//        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
//        /*
//         * Disable window framebuffer bits we don't need, because we render into offscreen FBO and blit to
//         * window.
//         */
//        glfwWindowHint(GLFW_DEPTH_BITS, 0);
//        glfwWindowHint(GLFW_STENCIL_BITS, 0);
////        glfwWindowHint(GLFW_ALPHA_BITS, 0);
//        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
//        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
////        glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_TRUE);
//        if (GameProperties.FULLSCREEN) {
//            glfwWindowHint(GLFW_FLOATING, GLFW_TRUE);
//            glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
//        }
//        if (GameProperties.DEBUG || GameProperties.GLDEBUG) {
//            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
//        }
//        long monitor = glfwGetPrimaryMonitor();
//        GLFWVidMode vidmode = glfwGetVideoMode(monitor);
//        width = (int) (Objects.requireNonNull(vidmode).width() * (GameProperties.FULLSCREEN ? 1 : 0.6f));
//        height = (int) (vidmode.height() * (GameProperties.FULLSCREEN ? 1 : 0.6f));
//        window = glfwCreateWindow(width, height, "Hello, voxel world!", GameProperties.FULLSCREEN ? monitor : NULL, NULL);
//        if (GameProperties.GRAB_CURSOR) {
//            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
//        }
//        if (window == NULL) {
//            throw new AssertionError("Failed to create the GLFW window");
//        }
//    }
//
//    private void setWindowPosition() {
//        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
//        glfwSetWindowPos(window, (Objects.requireNonNull(vidmode).width() - width) / 2, (vidmode.height() - height) / 2);
//    }
//
//    private void queryFramebufferSizeForHiDPI() {
//        try (MemoryStack frame = stackPush()) {
//            IntBuffer framebufferSize = frame.mallocInt(2);
//            nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
//            width = framebufferSize.get(0);
//            height = framebufferSize.get(1);
//        }
//    }
//
//    private void installDebugCallback() {
//        debugProc = setupDebugMessageCallback();
//        if (canUseSynchronousDebugCallback) {
//            glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS_ARB);
//        }
//    }
//
//    /**
//     * Set global GL state that will not be changed afterwards.
//     */
//    private void configureGlobalGlState() {
//        glClearColor(225 / 255f, 253 / 255f, 255 / 255f, 0f);
//        glEnable(GL_PRIMITIVE_RESTART);
//        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//        glPrimitiveRestartIndex(GameProperties.PRIMITIVE_RESTART_INDEX);
//        if (useInverseDepth) {
//            glClipControl(GL_LOWER_LEFT, GL_ZERO_TO_ONE);
//            glDepthFunc(GL_GREATER);
//            glClearDepth(0.0);
//        } else {
//            glDepthFunc(GL_LESS);
//        }
//    }
//
//    /**
//     * Query all (optional) capabilites/extensions that we want to use from the OpenGL context via
//     * LWJGL's {@link GLCapabilities}.
//     */
//    private void determineOpenGLCapabilities() {
//        caps = GL.createCapabilities();
//        useMultiDrawIndirect = caps.GL_ARB_multi_draw_indirect || caps.OpenGL43;
//        useBufferStorage = caps.GL_ARB_buffer_storage || caps.OpenGL44;
//        useClearBuffer = caps.GL_ARB_clear_buffer_object || caps.OpenGL43;
//        drawPointsWithGS = useMultiDrawIndirect; // <- we just haven't implemented point/GS rendering without MDI yet
//        useInverseDepth = caps.GL_ARB_clip_control || caps.OpenGL45;
//        useNvMultisampleCoverage = caps.GL_NV_framebuffer_multisample_coverage;
//        canUseSynchronousDebugCallback = caps.GL_ARB_debug_output || caps.OpenGL43;
//        canGenerateDrawCallsViaShader = caps.GL_ARB_shader_image_load_store/* 4.2 */ && caps.GL_ARB_shader_storage_buffer_object/* 4.3 */ && caps.GL_ARB_shader_atomic_counters/* 4.2 */ || caps.OpenGL43;
//        useOcclusionCulling = canGenerateDrawCallsViaShader && useMultiDrawIndirect;
//        useTemporalCoherenceOcclusionCulling = useOcclusionCulling && true;
//        canSourceIndirectDrawCallCountFromBuffer = canGenerateDrawCallsViaShader && (caps.GL_ARB_indirect_parameters || caps.OpenGL46);
//        useRepresentativeFragmentTest = caps.GL_NV_representative_fragment_test;
//        /* Query the necessary UBO alignment which we need for multi-buffering */
//        uniformBufferOffsetAlignment = glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);
//
//        System.out.println("useMultiDrawIndirect: " + useMultiDrawIndirect);
//        System.out.println("useBufferStorage: " + useBufferStorage);
//        System.out.println("drawPointsWithGS: " + drawPointsWithGS);
//        System.out.println("useInverseDepth: " + useInverseDepth);
//        System.out.println("useNvMultisampleCoverage: " + useNvMultisampleCoverage);
//        System.out.println("canUseSynchronousDebugCallback: " + canUseSynchronousDebugCallback);
//        System.out.println("canGenerateDrawCallsViaShader: " + canGenerateDrawCallsViaShader);
//        System.out.println("useOcclusionCulling: " + useOcclusionCulling);
//        System.out.println("useTemporalCoherenceOcclusionCulling: " + useTemporalCoherenceOcclusionCulling);
//        System.out.println("canSourceIndirectDrawCallCountFromBuffer: " + canSourceIndirectDrawCallCountFromBuffer);
//        System.out.println("useRepresentativeFragmentTest: " + useRepresentativeFragmentTest);
//        System.out.println("uniformBufferOffsetAlignment: " + uniformBufferOffsetAlignment);
//    }
//
//    /**
//     * We will render to an FBO.
//     */
//    private void createFramebufferObject() {
//        /*
//         * Delete any existing FBO (happens when we resize the window).
//         */
//        if (fbo != 0) {
//            glDeleteFramebuffers(fbo);
//            glDeleteRenderbuffers(new int[]{colorRbo, depthRbo});
//        }
//        fbo = glGenFramebuffers();
//        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
//        colorRbo = glGenRenderbuffers();
//        glBindRenderbuffer(GL_RENDERBUFFER, colorRbo);
//        if (useNvMultisampleCoverage) {
//            glRenderbufferStorageMultisampleCoverageNV(GL_RENDERBUFFER, GameProperties.COVERAGE_SAMPLES, GameProperties.COLOR_SAMPLES, GL_RGBA8, width, height);
//        } else {
//            glRenderbufferStorageMultisample(GL_RENDERBUFFER, GameProperties.COVERAGE_SAMPLES, GL_RGBA8, width, height);
//        }
//        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, colorRbo);
//        depthRbo = glGenRenderbuffers();
//        glBindRenderbuffer(GL_RENDERBUFFER, depthRbo);
//        if (useNvMultisampleCoverage) {
//            glRenderbufferStorageMultisampleCoverageNV(GL_RENDERBUFFER, GameProperties.COVERAGE_SAMPLES, GameProperties.COLOR_SAMPLES, GL_DEPTH_COMPONENT32F, width, height);
//        } else {
//            glRenderbufferStorageMultisample(GL_RENDERBUFFER, GameProperties.COVERAGE_SAMPLES, GL_DEPTH_COMPONENT32F, width, height);
//        }
//        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthRbo);
//        glBindRenderbuffer(GL_RENDERBUFFER, 0);
//        glBindFramebuffer(GL_FRAMEBUFFER, 0);
//    }
//
//    /**
//     * Create an empty VAO.
//     */
//    private void createNullVao() {
//        nullVao = glGenVertexArrays();
//    }
//
//    private void createBoundingBoxesVao() {
//        boundingBoxesVao = glGenVertexArrays();
//        glBindVertexArray(boundingBoxesVao);
//        boundingBoxesVertexBufferObject = glGenBuffers();
//        glBindBuffer(GL_ARRAY_BUFFER, boundingBoxesVertexBufferObject);
//        if (useBufferStorage) {
//            glBufferStorage(GL_ARRAY_BUFFER, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * GameProperties.MAX_ACTIVE_CHUNKS * 4 * Integer.BYTES, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT);
//            boundingBoxesVertexBufferObjectAddr = nglMapBufferRange(GL_ARRAY_BUFFER, 0L, GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * GameProperties.MAX_ACTIVE_CHUNKS * 4 * Integer.BYTES, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_FLUSH_EXPLICIT_BIT);
//        } else {
//            glBufferData(GL_ARRAY_BUFFER, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * GameProperties.MAX_ACTIVE_CHUNKS * 4 * Integer.BYTES, GL_STATIC_DRAW);
//        }
//        glEnableVertexAttribArray(0);
//        glBindVertexArray(0);
//    }
//
//    private void createGUIVao() {
//        guiVao = glGenVertexArrays();
//        glBindVertexArray(guiVao);
//        guiVertexBufferObject = glGenBuffers();
//        glBindBuffer(GL_ARRAY_BUFFER, guiVertexBufferObject);
////        if (useBufferStorage) {
////            glBufferStorage(GL_ARRAY_BUFFER, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * GameProperties.MAX_ACTIVE_CHUNKS * 4 * Integer.BYTES, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT);
////            boundingBoxesVertexBufferObjectAddr = nglMapBufferRange(GL_ARRAY_BUFFER, 0L, GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * GameProperties.MAX_ACTIVE_CHUNKS * 4 * Integer.BYTES, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_FLUSH_EXPLICIT_BIT);
////        } else {
//        glBufferData(GL_ARRAY_BUFFER, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * 4 * Integer.BYTES, GL_STATIC_DRAW);
////        }
//        glEnableVertexAttribArray(0);
//        glBindVertexArray(0);
//    }
//
//
//    /**
//     * Create some pre-defined materials.
//     */
//    private void createMaterials() {
//        materials = new Material[]{
//                new Material(rgb(0, 0, 0)),
//                new Material(rgb(46, 213, 64)),
//                new Material(rgb(255, 0, 0)),
//
//                new Material(rgb(255, 115, 0)),
//
//                new Material(rgb(252, 252, 0)),
//
//                new Material(rgb(0, 255, 255)),
//
//                new Material(rgb(63, 0, 255)),
//
//                new Material(rgb(76, 50, 176)),
//
//                new Material(rgb(2, 3, 3)),
//
//                new Material(rgb(255, 255, 255)),
//
//                new Material(rgb(42, 45, 46)),
//                new Material(rgb(157, 12, 205)),
//                new Material(rgb(71, 28, 19)),
//                new Material(rgb(30, 166, 154)),
//                new Material(rgb(8, 45, 61)),
//                new Material(rgb(11, 61, 3)),
//                new Material(rgb(75, 75, 124)),
//                new Material(rgb(28, 180, 133))
//
//        };
//        createMaterialsTexture();
//    }
//
//
//    /**
//     * Create the shader program used to render the chunks.
//     */
//    private void createChunksProgram() throws IOException {
//        Map<String, String> defines = new HashMap<>();
//        defines.put("AO_FACTORS", GameProperties.AO_FACTORS);
//        defines.put("MDI", useMultiDrawIndirect ? "1" : "0");
//        int program = glCreateProgram();
//        int vshader = createShader("org/lwjgl/demo/mikstcraft/voxelgame/chunk" + (drawPointsWithGS ? "-points" : "") + ".vs.glsl", GL_VERTEX_SHADER, defines);
//        int gshader = 0;
//        if (drawPointsWithGS) {
//            gshader = createShader("org/lwjgl/demo/mikstcraft/voxelgame/chunk.gs.glsl", GL_GEOMETRY_SHADER, defines);
//        }
//        int fshader = createShader("org/lwjgl/demo/mikstcraft/voxelgame/chunk.fs.glsl", GL_FRAGMENT_SHADER, defines);
//        glAttachShader(program, vshader);
//        if (drawPointsWithGS) {
//            glAttachShader(program, gshader);
//        }
//        glAttachShader(program, fshader);
//        glLinkProgram(program);
//        glDeleteShader(vshader);
//        if (drawPointsWithGS) {
//            glDeleteShader(gshader);
//        }
//        glDeleteShader(fshader);
//        if (GameProperties.DEBUG || GameProperties.GLDEBUG) {
//            int linked = glGetProgrami(program, GL_LINK_STATUS);
//            String programLog = glGetProgramInfoLog(program);
//            if (programLog.trim().length() > 0) {
//                System.err.println(programLog);
//            }
//            if (linked == 0) {
//                throw new AssertionError("Could not link program");
//            }
//        }
//        glUseProgram(program);
//        chunksProgramUboBlockIndex = glGetUniformBlockIndex(program, "Uniforms");
//
//        glUniform1i(glGetUniformLocation(program, "materials"), 0);
//        glUniform1i(glGetUniformLocation(program, "chunkInfo"), 1);
//
//        int texLoc = glGetUniformLocation(program, "chunkTex");
//        glActiveTexture(GL_TEXTURE2);
//        glBindTexture(GL_TEXTURE_2D, grassTextureId);
//        glUniform1i(texLoc, 2);
//
//
//        glUseProgram(0);
////        glBindTexture(GL_TEXTURE_2D, 0);
////        glBindTexture(GL_TEXTURE_2D, 0);
//        chunksProgram = program;
//    }
//
//    /**
//     * Create the (multi-buffered) uniform buffer object to hold uniforms needed by the chunks program.
//     */
//    private void createChunksProgramUbo() {
//        chunksProgramUbo = glGenBuffers();
//        glBindBuffer(GL_UNIFORM_BUFFER, chunksProgramUbo);
//        int size = roundUpToNextMultiple(chunksProgramUboSize, uniformBufferOffsetAlignment);
//        if (useBufferStorage) {
//            glBufferStorage(GL_UNIFORM_BUFFER, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * size, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT);
//            chunksProgramUboAddr = nglMapBufferRange(GL_UNIFORM_BUFFER, 0L, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * size, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_FLUSH_EXPLICIT_BIT);
//        } else {
//            glBufferData(GL_UNIFORM_BUFFER, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * size, GL_DYNAMIC_DRAW);
//        }
//        glBindBuffer(GL_UNIFORM_BUFFER, 0);
//    }
//
//    /**
//     * Create the UBO for the bounding boxes program.
//     */
//    private void createBoundingBoxesProgramUbo() {
//        boundingBoxesProgramUbo = glGenBuffers();
//        glBindBuffer(GL_UNIFORM_BUFFER, boundingBoxesProgramUbo);
//        int size = roundUpToNextMultiple(boundingBoxesProgramUboSize, uniformBufferOffsetAlignment);
//        if (useBufferStorage) {
//            glBufferStorage(GL_UNIFORM_BUFFER, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * size, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT);
//            boundingBoxesProgramUboAddr = nglMapBufferRange(GL_UNIFORM_BUFFER, 0L, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * size, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_FLUSH_EXPLICIT_BIT);
//        } else {
//            glBufferData(GL_UNIFORM_BUFFER, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * size, GL_DYNAMIC_DRAW);
//        }
//        glBindBuffer(GL_UNIFORM_BUFFER, 0);
//    }
//
//    /**
//     * Create the shader program used to render the selection rectangle.
//     */
//    private void createSelectionProgram() throws IOException {
//        int program = glCreateProgram();
//        int vshader = createShader("org/lwjgl/demo/mikstcraft/voxelgame/selection.vs.glsl", GL_VERTEX_SHADER, Collections.emptyMap());
//        int fshader = createShader("org/lwjgl/demo/mikstcraft/voxelgame/selection.fs.glsl", GL_FRAGMENT_SHADER, Collections.emptyMap());
//        glAttachShader(program, vshader);
//        glAttachShader(program, fshader);
//        glLinkProgram(program);
//        if (GameProperties.DEBUG || GameProperties.GLDEBUG) {
//            int linked = glGetProgrami(program, GL_LINK_STATUS);
//            String programLog = glGetProgramInfoLog(program);
//            if (programLog.trim().length() > 0) {
//                System.err.println(programLog);
//            }
//            if (linked == 0) {
//                throw new AssertionError("Could not link program");
//            }
//        }
//        glUseProgram(program);
//        selectionProgramUboBlockIndex = glGetUniformBlockIndex(program, "Uniforms");
//        glUseProgram(0);
//        selectionProgram = program;
//    }
//
//    /**
//     * Create buffer objects for occlusion culling, such as:
//     * <ul>
//     * <li>SSBO as a visibility buffer to store a flag whether a given chunk's bounding box is visible
//     * or not
//     * <li>Atomic counter buffer for collecting MDI structs for visible chunks
//     * <li>SSBO to hold the final MDI structs for drawing the scene
//     * <li>if temporal coherence occlusion culling: Another SSBO to hold MDI structs for drawing newly
//     * disoccluded chunks
//     * </ul>
//     */
//    private void createOcclusionCullingBufferObjects() {
//        /*
//         * We need a "visibility flags" buffer to remember which of the in-frustum chunks are visible in any
//         * frame.
//         */
//        visibilityFlagsBuffer = glGenBuffers();
//        glBindBuffer(GL_SHADER_STORAGE_BUFFER, visibilityFlagsBuffer);
//        /* Allocate buffer storage and pre-initialize with zeroes. */
//        if (useBufferStorage) {
//            /*
//             * If we can't use ARB_clear_buffer_object, then it must be a dynamic storage buffer for client-side
//             * uploads to clear the buffer.
//             */
//            glBufferStorage(GL_SHADER_STORAGE_BUFFER, GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * GameProperties.MAX_ACTIVE_CHUNKS * Integer.BYTES, useClearBuffer ? 0 : GL_DYNAMIC_STORAGE_BIT);
//        } else {
//            glBufferData(GL_SHADER_STORAGE_BUFFER, GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * GameProperties.MAX_ACTIVE_CHUNKS * Integer.BYTES, GL_DYNAMIC_DRAW);
//        }
//        /* We need an atomic counter (to collect MDI draw structs for visible chunks) */
//        atomicCounterBuffer = glGenBuffers();
//        glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, atomicCounterBuffer);
//        /*
//         * If we use temporal coherence occlusion culling, we need twice as many atomics, because we need
//         * one to count the MDI draws for all non-occluded in-frustum chunks per frame as well as one for
//         * newly disoccluded chunks per frame.
//         */
//        long multiplier = useTemporalCoherenceOcclusionCulling ? 2 : 1;
//        int atomicCounterBufferIntSize = (int) (multiplier * GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS);
//        try (MemoryStack stack = stackPush()) {
//            /* Allocate buffer storage and pre-initialize with zeroes. */
//            if (useBufferStorage) {
//                /*
//                 * If we can't use ARB_clear_buffer_object, then it must be a dynamic storage buffer for client-side
//                 * uploads.
//                 */
//                glBufferStorage(GL_ATOMIC_COUNTER_BUFFER, stack.callocInt(atomicCounterBufferIntSize), useClearBuffer ? 0 : GL_DYNAMIC_STORAGE_BIT);
//            } else {
//                glBufferData(GL_ATOMIC_COUNTER_BUFFER, stack.callocInt(atomicCounterBufferIntSize), GL_DYNAMIC_DRAW);
//            }
//        }
//        /* and we need an "indirect" buffer to store the MDI draw structs into */
//        indirectDrawCulledBuffer = glGenBuffers();
//        glBindBuffer(GL_SHADER_STORAGE_BUFFER, indirectDrawCulledBuffer);
//        /*
//         * If we use temporal coherence occlusion culling, we need twice the buffer size for MDI draw
//         * structs, because we need a region to hold the MDI draws for all non-occluded chunks per frame as
//         * well as one for newly disoccluded chunks per frame.
//         */
//        long indirectDrawCulledBufferSize = multiplier * GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * GameProperties.MAX_ACTIVE_CHUNKS * 5 * Integer.BYTES;
//        if (useBufferStorage) {
//            glBufferStorage(GL_SHADER_STORAGE_BUFFER, indirectDrawCulledBufferSize, 0);
//        } else {
//            glBufferData(GL_SHADER_STORAGE_BUFFER, indirectDrawCulledBufferSize, GL_DYNAMIC_DRAW);
//        }
//    }
//
//    /**
//     * Create a program used to draw chunks' bounding boxes by expanding points to cubes in a geometry
//     * shader.
//     */
//    private void createBoundingBoxesProgram() throws IOException {
//        Map<String, String> defines = new HashMap<>();
//        /* The geometry shader needs to know the chunk size to scale the base axes vectors */
//        defines.put("CHUNK_SIZE", Integer.toString(GameProperties.CHUNK_SIZE));
//        int program = glCreateProgram();
//        int vshader = createShader("org/lwjgl/demo/mikstcraft/voxelgame/boundingboxes.vs.glsl", GL_VERTEX_SHADER, defines);
//        int gshader = createShader("org/lwjgl/demo/mikstcraft/voxelgame/boundingboxes.gs.glsl", GL_GEOMETRY_SHADER, defines);
//        int fshader = createShader("org/lwjgl/demo/mikstcraft/voxelgame/boundingboxes.fs.glsl", GL_FRAGMENT_SHADER, defines);
//        glAttachShader(program, vshader);
//        glAttachShader(program, gshader);
//        glAttachShader(program, fshader);
//        glLinkProgram(program);
//        glDeleteShader(vshader);
//        glDeleteShader(gshader);
//        glDeleteShader(fshader);
//        if (GameProperties.DEBUG || GameProperties.GLDEBUG) {
//            int linked = glGetProgrami(program, GL_LINK_STATUS);
//            String programLog = glGetProgramInfoLog(program);
//            if (programLog.trim().length() > 0) {
//                System.err.println(programLog);
//            }
//            if (linked == 0) {
//                throw new AssertionError("Could not link program");
//            }
//        }
//        glUseProgram(program);
//        boundingBoxesProgramUboBlockIndex = glGetUniformBlockIndex(program, "Uniforms");
//        glUseProgram(0);
//        boundingBoxesProgram = program;
//    }
//
//    /**
//     * Create a program used to draw chunks' bounding boxes by expanding points to cubes in a geometry
//     * shader.
//     */
//    private void createGUIProgram() throws IOException {
//        Map<String, String> defines = new HashMap<>();
//
//        int program = glCreateProgram();
//        int vshader = createShader("org/lwjgl/demo/mikstcraft/voxelgame/gui.vs.glsl", GL_VERTEX_SHADER, defines);
////        int gshader = createShader("org/lwjgl/demo/mikstcraft/voxelgame/gui.gs.glsl", GL_GEOMETRY_SHADER, defines);
//        int fshader = createShader("org/lwjgl/demo/mikstcraft/voxelgame/gui.fs.glsl", GL_FRAGMENT_SHADER, defines);
//        glAttachShader(program, vshader);
////        glAttachShader(program, gshader);
//        glAttachShader(program, fshader);
//        glLinkProgram(program);
//        glDeleteShader(vshader);
////        glDeleteShader(gshader);
//        glDeleteShader(fshader);
//        if (GameProperties.DEBUG || GameProperties.GLDEBUG) {
//            int linked = glGetProgrami(program, GL_LINK_STATUS);
//            String programLog = glGetProgramInfoLog(program);
//            if (programLog.trim().length() > 0) {
//                System.err.println(programLog);
//            }
//            if (linked == 0) {
//                throw new AssertionError("Could not link program");
//            }
//        }
//        glUseProgram(program);
//        guiProgramUboBlockIndex = glGetUniformBlockIndex(program, "Uniforms");
//
//        glUseProgram(0);
//        guiProgram = program;
//    }
//
//    /**
//     * Create the vertex-shader-only program to generate MDI calls into an output SSBO from all chunks'
//     * MDI calls in an input SSBO depending on the visibility flag generated by the bounding boxes draw.
//     */
//    private void createCollectDrawCallsProgram() throws IOException {
//        Map<String, String> defines = new HashMap<>();
//        defines.put("VERTICES_PER_FACE", PerFaceBuffers.verticesPerFace + "u");
//        defines.put("INDICES_PER_FACE", PerFaceBuffers.indicesPerFace + "u");
//        defines.put("TEMPORAL_COHERENCE", useTemporalCoherenceOcclusionCulling ? "1" : "0");
//        int program = glCreateProgram();
//        int vshader = createShader("org/lwjgl/demo/mikstcraft/voxelgame/collectdrawcalls.vs.glsl", GL_VERTEX_SHADER, defines);
//        glAttachShader(program, vshader);
//        glLinkProgram(program);
//        glDeleteShader(vshader);
//        if (GameProperties.DEBUG || GameProperties.GLDEBUG) {
//            int linked = glGetProgrami(program, GL_LINK_STATUS);
//            String programLog = glGetProgramInfoLog(program);
//            if (programLog.trim().length() > 0) {
//                System.err.println(programLog);
//            }
//            if (linked == 0) {
//                throw new AssertionError("Could not link program");
//            }
//        }
//        collectDrawCallsProgram = program;
//    }
//
//    /**
//     * Round the <em>positive</em> number <code>num</code> up to be a multiple of <code>factor</code>.
//     */
//    private static int roundUpToNextMultiple(int num, int factor) {
//        return num + factor - 1 - (num + factor - 1) % factor;
//    }
//
//    /**
//     * Create the (multi-buffered) uniform buffer object to hold uniforms needed by the selection
//     * program.
//     */
//    private void createSelectionProgramUbo() {
//        selectionProgramUbo = glGenBuffers();
//        glBindBuffer(GL_UNIFORM_BUFFER, selectionProgramUbo);
//        int size = roundUpToNextMultiple(selectionProgramUboSize, uniformBufferOffsetAlignment);
//        if (useBufferStorage) {
//            glBufferStorage(GL_UNIFORM_BUFFER, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * size, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT);
//            selectionProgramUboAddr = nglMapBufferRange(GL_UNIFORM_BUFFER, 0L, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * size, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_FLUSH_EXPLICIT_BIT);
//        } else {
//            glBufferData(GL_UNIFORM_BUFFER, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * size, GL_DYNAMIC_DRAW);
//        }
//        glBindBuffer(GL_UNIFORM_BUFFER, 0);
//    }
//
//    /**
//     * Create the (multi-buffered) uniform buffer object to hold uniforms needed by the selection
//     * program.
//     */
//    private void createGUIProgramUbo() {
//        guiProgramUbo = glGenBuffers();
//        glBindBuffer(GL_UNIFORM_BUFFER, guiProgramUbo);
//        int size = roundUpToNextMultiple(selectionProgramUboSize, uniformBufferOffsetAlignment);
////        if (useBufferStorage) {
////            glBufferStorage(GL_UNIFORM_BUFFER, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * size, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT);
////            selectionProgramUboAddr = nglMapBufferRange(GL_UNIFORM_BUFFER, 0L, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * size, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_FLUSH_EXPLICIT_BIT);
////        } else {
//        glBufferData(GL_UNIFORM_BUFFER, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * size, GL_DYNAMIC_DRAW);
////        }
//        glBindBuffer(GL_UNIFORM_BUFFER, 0);
//    }
//
//
//    private void createMaterialsTexture() {
//        int materialsBufferObject = glGenBuffers();
//        try (MemoryStack stack = stackPush()) {
//            long materialsBuffer = stack.nmalloc(Integer.BYTES * materials.length);
//            for (int i = 0; i < materials.length; i++) {
//                Material mat = materials[i];
//                memPutInt(materialsBuffer + i * Integer.BYTES, mat == null ? 0 : mat.col);
//            }
//            glBindBuffer(GL_TEXTURE_BUFFER, materialsBufferObject);
//            if (useBufferStorage) {
//                nglBufferStorage(GL_TEXTURE_BUFFER, materials.length * Integer.BYTES, materialsBuffer, 0);
//            } else {
//                nglBufferData(GL_TEXTURE_BUFFER, materials.length * Integer.BYTES, materialsBuffer, GL_STATIC_DRAW);
//            }
//        }
//        glBindBuffer(GL_TEXTURE_BUFFER, 0);
//        materialsTexture = glGenTextures();
//        glBindTexture(GL_TEXTURE_BUFFER, materialsTexture);
//        glTexBuffer(GL_TEXTURE_BUFFER, GL_RGBA8, materialsBufferObject);
//        glBindTexture(GL_TEXTURE_BUFFER, 0);
//    }
//
//    private void handleKeyboardInput() {
//        float factor = fly ? 40f : 15f;
//        float dangz = 0.04f;
//        float maxangz = 0.2f;
//
//        player.getVelocity().x = 0f;
//        if (fly) {
//            player.getVelocity().y = 0f;
//        }
//        player.getVelocity().z = 0f;
//        if (keydown[GLFW_KEY_LEFT_SHIFT]) {
//            factor = fly ? 180f : 140f;
//        }
//        if (keydown[GLFW_KEY_W]) {
//            player.getVelocity().sub(vMat.positiveZ(tmpv3f).mul(factor, fly ? factor : 0, factor));
//        }
//        if (keydown[GLFW_KEY_S]) {
//            player.getVelocity().add(vMat.positiveZ(tmpv3f).mul(factor, fly ? factor : 0, factor));
//        }
//        if (keydown[GLFW_KEY_A]) {
//            angz -= dangz * 2;
//
//            player.getVelocity().sub(vMat.positiveX(tmpv3f).mul(factor, fly ? factor : 0, factor));
//        }
//        if (keydown[GLFW_KEY_D]) {
//            angz += dangz * 2;
//
//            player.getVelocity().add(vMat.positiveX(tmpv3f).mul(factor, fly ? factor : 0, factor));
//        }
//        if (keydown[GLFW_KEY_SPACE] && fly) {
//            player.getVelocity().add(vMat.positiveY(tmpv3f).mul(fly ? factor : 1));
//        }
//        if (keydown[GLFW_KEY_LEFT_CONTROL] && fly) {
//            player.getVelocity().sub(vMat.positiveY(tmpv3f).mul(fly ? factor : 0));
//        }
//        if (!fly && keydown[GLFW_KEY_SPACE] && !jumping) {
//            jumping = true;
//            player.getVelocity().add(0, 13, 0);
//        } else if (!keydown[GLFW_KEY_SPACE]) {
//            jumping = false;
//        }
//        if (angz < 0) {
//            angz += dangz;
//        }
//        if (angz > 0) {
//            angz -= dangz;
//        }
//        if (angz > maxangz) {
//            angz = maxangz;
//        }
//        if (angz < -maxangz) {
//            angz = -maxangz;
//        }
//    }
//
//    private void updatePlayerPositionAndMatrices(float dt) {
//        handleKeyboardInput();
//        angx += dangx * 0.002f;
//        angy += dangy * 0.002f;
//        dangx *= 0.0994f;
//        dangy *= 0.0994f;
//        if (!fly) {
//            player.getVelocity().add(player.getAcceleration().mul(dt, tmpv3f));
//            handleCollisions(dt, player.getVelocity(), player.getPosition());
//        } else {
//            player.getPosition().add(player.getVelocity().mul(dt, tmpv3f));
//        }
//        vMat.rotation(tmpq.rotationX(angx).rotateY(angy).rotateLocalZ(angz));
//        //.rotateZ(angz));
////        vMat.rotation(angz,)
//        vMat.translate((float) -(player.getPosition().x - floor(player.getPosition().x)), (float) -(player.getPosition().y - floor(player.getPosition().y)), (float) -(player.getPosition().z - floor(player.getPosition().z)));
//        pMat.setPerspective((float) toRadians(GameProperties.FOV_DEGREES), (float) width / height, useInverseDepth ? GameProperties.FAR : GameProperties.NEAR, useInverseDepth ? GameProperties.NEAR : GameProperties.FAR, useInverseDepth);
//        pMat.mulPerspectiveAffine(vMat, mvpMat);
//        mvpMat.invert(imvpMat);
//        updateFrustumPlanes();
//    }
//
//    /**
//     * Update the plane equation coefficients for the frustum planes from the {@link #mvpMat}.
//     */
//    private void updateFrustumPlanes() {
//        Matrix4f m = mvpMat;
//        FrustumPlanes frustumPlanes = renderedWorld.getFrustumPlanes();
//
//        frustumPlanes.nxX = m.m03() + m.m00();
//        frustumPlanes.nxY = m.m13() + m.m10();
//        frustumPlanes.nxZ = m.m23() + m.m20();
//        frustumPlanes.nxW = m.m33() + m.m30();
//        frustumPlanes.pxX = m.m03() - m.m00();
//        frustumPlanes.pxY = m.m13() - m.m10();
//        frustumPlanes.pxZ = m.m23() - m.m20();
//        frustumPlanes.pxW = m.m33() - m.m30();
//        frustumPlanes.nyX = m.m03() + m.m01();
//        frustumPlanes.nyY = m.m13() + m.m11();
//        frustumPlanes.nyZ = m.m23() + m.m21();
//        frustumPlanes.nyW = m.m33() + m.m31();
//        frustumPlanes.pyX = m.m03() - m.m01();
//        frustumPlanes.pyY = m.m13() - m.m11();
//        frustumPlanes.pyZ = m.m23() - m.m21();
//        frustumPlanes.pyW = m.m33() - m.m31();
//    }
//
//
//    /**
//     * Determine whether the player's eye is currently inside the given chunk.
//     */
//    private boolean playerInsideChunk(Chunk chunk) {
//        float margin = GameProperties.CHUNK_SIZE * 0.5f;
//        int minX = chunk.cx << GameProperties.CHUNK_SIZE_SHIFT, maxX = minX + GameProperties.CHUNK_SIZE;
//        int minZ = chunk.cz << GameProperties.CHUNK_SIZE_SHIFT, maxZ = minZ + GameProperties.CHUNK_SIZE;
//        return player.getPosition().x + margin >= minX && player.getPosition().x - margin <= maxX && player.getPosition().z + margin >= minZ && player.getPosition().z - margin <= maxZ;
//    }
//
//
//    /**
//     * Handle any collisions with the player and the voxels.
//     */
//    private void handleCollisions(float dt, Vector3f v, Vector3d p) {
//        List<Contact> contacts = new ArrayList<>();
//        collisionDetection(dt, v, contacts);
//        collisionResponse(dt, v, p, contacts);
//    }
//
//    /**
//     * Detect possible collision candidates.
//     */
//    private void collisionDetection(float dt, Vector3f v, List<Contact> contacts) {
//        float dx = v.x * dt, dy = v.y * dt, dz = v.z * dt;
//        int minX = (int) floor(player.getPosition().x - GameProperties.PLAYER_WIDTH + (dx < 0 ? dx : 0));
//        int maxX = (int) floor(player.getPosition().x + GameProperties.PLAYER_WIDTH + (dx > 0 ? dx : 0));
//        int minY = (int) floor(player.getPosition().y - GameProperties.PLAYER_EYE_HEIGHT + (dy < 0 ? dy : 0));
//        int maxY = (int) floor(player.getPosition().y + GameProperties.PLAYER_HEIGHT - GameProperties.PLAYER_EYE_HEIGHT + (dy > 0 ? dy : 0));
//        int minZ = (int) floor(player.getPosition().z - GameProperties.PLAYER_WIDTH + (dz < 0 ? dz : 0));
//        int maxZ = (int) floor(player.getPosition().z + GameProperties.PLAYER_WIDTH + (dz > 0 ? dz : 0));
//        /* Just loop over all voxels that could possibly collide with the player */
//        for (int y = min(GameProperties.CHUNK_HEIGHT - 1, maxY); y >= 0 && y >= minY; y--) {
//            for (int z = minZ; z <= maxZ; z++) {
//                for (int x = minX; x <= maxX; x++) {
//                    if (load(x, y, z) == GameProperties.EMPTY_VOXEL) {
//                        continue;
//                    }
//                    /* and perform swept-aabb intersection */
//                    intersectSweptAabbAabb(x, y, z, (float) (player.getPosition().x - x), (float) (player.getPosition().y - y), (float) (player.getPosition().z - z), dx, dy, dz, contacts);
//                }
//            }
//        }
//    }
//
//    /**
//     * Compute the exact collision point between the player and the voxel at <code>(x, y, z)</code>.
//     */
//    private void intersectSweptAabbAabb(int x, int y, int z, float px, float py, float pz, float dx, float dy,
//                                        float dz, List<Contact> contacts) {
//        /*
//         * https://www.gamedev.net/tutorials/programming/general-and-gameplay-programming/swept-aabb-
//         * collision-detection-and-response-r3084/
//         */
//        float pxmax = px + GameProperties.PLAYER_WIDTH, pxmin = px - GameProperties.PLAYER_WIDTH, pymax = py + GameProperties.PLAYER_HEIGHT - GameProperties.PLAYER_EYE_HEIGHT, pymin = py - GameProperties.PLAYER_EYE_HEIGHT, pzmax = pz + GameProperties.PLAYER_WIDTH, pzmin = pz - GameProperties.PLAYER_WIDTH;
//        float xInvEntry = dx > 0f ? -pxmax : 1 - pxmin, xInvExit = dx > 0f ? 1 - pxmin : -pxmax;
//        boolean xNotValid = dx == 0 || load(x + (dx > 0 ? -1 : 1), y, z) != GameProperties.EMPTY_VOXEL;
//        float xEntry = xNotValid ? NEGATIVE_INFINITY : xInvEntry / dx, xExit = xNotValid ? POSITIVE_INFINITY : xInvExit / dx;
//        float yInvEntry = dy > 0f ? -pymax : 1 - pymin, yInvExit = dy > 0f ? 1 - pymin : -pymax;
//        boolean yNotValid = dy == 0 || load(x, y + (dy > 0 ? -1 : 1), z) != GameProperties.EMPTY_VOXEL;
//        float yEntry = yNotValid ? NEGATIVE_INFINITY : yInvEntry / dy, yExit = yNotValid ? POSITIVE_INFINITY : yInvExit / dy;
//        float zInvEntry = dz > 0f ? -pzmax : 1 - pzmin, zInvExit = dz > 0f ? 1 - pzmin : -pzmax;
//        boolean zNotValid = dz == 0 || load(x, y, z + (dz > 0 ? -1 : 1)) != GameProperties.EMPTY_VOXEL;
//        float zEntry = zNotValid ? NEGATIVE_INFINITY : zInvEntry / dz, zExit = zNotValid ? POSITIVE_INFINITY : zInvExit / dz;
//        float tEntry = max(max(xEntry, yEntry), zEntry), tExit = min(min(xExit, yExit), zExit);
//        if (tEntry < -.5f || tEntry > tExit) {
//            return;
//        }
//        Contact c;
//        contacts.add(c = new Contact(tEntry, x, y, z));
//        if (xEntry == tEntry) {
//            c.nx = dx > 0 ? -1 : 1;
//        } else if (yEntry == tEntry) {
//            c.ny = dy > 0 ? -1 : 1;
//        } else {
//            c.nz = dz > 0 ? -1 : 1;
//        }
//    }
//
//    /**
//     * Respond to all found collision contacts.
//     */
//    private void collisionResponse(float dt, Vector3f v, Vector3d p, List<Contact> contacts) {
//        sort(contacts);
//        int minX = Integer.MIN_VALUE, maxX = Integer.MAX_VALUE, minY = Integer.MIN_VALUE, maxY = Integer.MAX_VALUE, minZ = Integer.MIN_VALUE, maxZ = Integer.MAX_VALUE;
//        float elapsedTime = 0f;
//        float dx = v.x * dt, dy = v.y * dt, dz = v.z * dt;
//        for (int i = 0; i < contacts.size(); i++) {
//            Contact contact = contacts.get(i);
//            if (contact.x <= minX || contact.y <= minY || contact.z <= minZ || contact.x >= maxX || contact.y >= maxY || contact.z >= maxZ) {
//                continue;
//            }
//            float t = contact.t - elapsedTime;
//            p.add(dx * t, dy * t, dz * t);
//            elapsedTime += t;
//            if (contact.nx != 0) {
//                minX = dx < 0 ? max(minX, contact.x) : minX;
//                maxX = dx < 0 ? maxX : min(maxX, contact.x);
//                v.x = 0f;
//                dx = 0f;
//            } else if (contact.ny != 0) {
//                minY = dy < 0 ? max(minY, contact.y) : contact.y - (int) GameProperties.PLAYER_HEIGHT;
//                maxY = dy < 0 ? contact.y + (int) ceil(GameProperties.PLAYER_HEIGHT) + 1 : min(maxY, contact.y);
//                v.y = 0f;
//                dy = 0f;
//            } else if (contact.nz != 0) {
//                minZ = dz < 0 ? max(minZ, contact.z) : minZ;
//                maxZ = dz < 0 ? maxZ : min(maxZ, contact.z);
//                v.z = 0f;
//                dz = 0f;
//            }
//        }
//        float trem = 1f - elapsedTime;
//        p.add(dx * trem, dy * trem, dz * trem);
//    }
//
//    /**
//     * Setup GL state prior to drawing the chunk's bounding boxes invisibly, but still tested against
//     * the current depth buffer.
//     */
//    private void preDrawBoundingBoxesForVisibilityBufferState() {
//        glDisable(GL_CULL_FACE);
//        glEnable(GL_DEPTH_TEST);
//        glDepthMask(false);
//        glColorMask(debugBoundingBoxes, debugBoundingBoxes, debugBoundingBoxes, debugBoundingBoxes);
//        if (useRepresentativeFragmentTest) {
//            glEnable(GL_REPRESENTATIVE_FRAGMENT_TEST_NV);
//        }
//        glEnable(GL_POLYGON_OFFSET_FILL);
//        glPolygonOffset(useInverseDepth ? 1 : -1, useInverseDepth ? 1 : -1);
//        glBindVertexArray(boundingBoxesVao);
//        glUseProgram(boundingBoxesProgram);
//        glBindBufferRange(GL_SHADER_STORAGE_BUFFER, 0, visibilityFlagsBuffer, (long) currentDynamicBufferIndex * GameProperties.MAX_ACTIVE_CHUNKS * Integer.BYTES, GameProperties.MAX_ACTIVE_CHUNKS * Integer.BYTES);
//        int uboSize = roundUpToNextMultiple(boundingBoxesProgramUboSize, uniformBufferOffsetAlignment);
//        glBindBufferRange(GL_UNIFORM_BUFFER, boundingBoxesProgramUboBlockIndex, boundingBoxesProgramUbo, (long) currentDynamicBufferIndex * uboSize, uboSize);
//    }
//
//    /**
//     * Draw all in-frustum chunks' bounding boxes.
//     * <p>
//     * This is to test those bounding boxes against the current depth buffer and, if any fragment gets
//     * generated, write a visibility flag to an SSBO for that chunk.
//     * <p>
//     * We also always flag a chunk to be visible when we MUST draw it, because the player's eye location
//     * is inside of the chunk and with the bounding box not being visible.
//     * <p>
//     * This also makes use of NV_representative_fragment_test (if available) to generate fewer fragments
//     * (and thus perform fewer SSBO writes).
//     */
//    private void drawBoundingBoxesOfInFrustumChunks() {
//        /*
//         * Fill buffer objects to draw in-frustum chunks' bounding boxes.
//         */
//        updateBoundingBoxesInputBuffersForInFrustumChunks();
//        /*
//         * Update the uniform buffer object for drawing the bounding boxes.
//         */
//        updateBoundingBoxesProgramUbo();
//        /*
//         * Setup (global) OpenGL state for drawing the bounding boxes.
//         */
//        preDrawBoundingBoxesForVisibilityBufferState();
//        /*
//         * Clear the flags in the visibility buffer. This buffer will then be filled in the fragment shader
//         * for all visible chunks.
//         */
//        try (MemoryStack stack = stackPush()) {
//            long clearOffset = (long) currentDynamicBufferIndex * GameProperties.MAX_ACTIVE_CHUNKS * Integer.BYTES;
//            long clearSize = (long) GameProperties.MAX_ACTIVE_CHUNKS * Integer.BYTES;
//            if (useClearBuffer) {
//                glClearBufferSubData(GL_SHADER_STORAGE_BUFFER, GL_R32UI, clearOffset, clearSize, GL_RED_INTEGER, GL_UNSIGNED_INT, stack.ints(0));
//            } else {
////                glBufferSubData(GL_SHADER_STORAGE_BUFFER, clearOffset, stack.callocInt((int) (clearSize / Integer.BYTES)));
//            }
//        }
//        /*
//         * Start drawing from the correct array buffer offset/region. We don't use the 'firstVertex' in the
//         * glDrawArrays() call but offset the vertex attrib pointer, because we want gl_VertexID to start
//         * from zero in the shader later.
//         */
//        long vertexByteOffset = (long) currentDynamicBufferIndex * GameProperties.MAX_ACTIVE_CHUNKS * 4 * Integer.BYTES;
//        glVertexAttribIPointer(0, 4, GL_UNSIGNED_INT, 0, vertexByteOffset);
//        glDrawArrays(GL_POINTS, 0, numChunksInFrustum);
//        postDrawBoundingBoxesForVisibilityBufferState();
//    }
//
//    /**
//     * Reset of critical global GL state, that we cannot assume the next draw call to reset itself,
//     * after {@link #drawBoundingBoxesOfInFrustumChunks()}.
//     */
//    private void postDrawBoundingBoxesForVisibilityBufferState() {
//        if (useRepresentativeFragmentTest) {
//            glDisable(GL_REPRESENTATIVE_FRAGMENT_TEST_NV);
//        }
//        glDisable(GL_POLYGON_OFFSET_FILL);
//        glPolygonOffset(0, 0);
//        glEnable(GL_DEPTH_TEST);
//        glDepthMask(true);
//        glColorMask(true, true, true, true);
//    }
//
//    /**
//     * Setup global GL state prior to collecting effective MDI structs via
//     * {@link #collectDrawCommands()}.
//     */
//    private void preCollectDrawCommandsState() {
//        glEnable(GL_RASTERIZER_DISCARD);
//        glBindVertexArray(boundingBoxesVao);
//        glUseProgram(collectDrawCallsProgram);
//        /* Bind atomic counter for counting _all_ non-occluded in-frustum chunks */
//        glBindBufferRange(GL_ATOMIC_COUNTER_BUFFER, 0, atomicCounterBuffer, (long) currentDynamicBufferIndex * Integer.BYTES, Integer.BYTES);
//        clearAtomicCounter(0);
//        /* Bind buffer for the visibility flags of non-occluded in-frustum chunks */
//        glBindBufferRange(GL_SHADER_STORAGE_BUFFER, 0, visibilityFlagsBuffer, (long) currentDynamicBufferIndex * GameProperties.MAX_ACTIVE_CHUNKS * Integer.BYTES, (long) GameProperties.MAX_ACTIVE_CHUNKS * Integer.BYTES);
//        /* Bind buffer for MDI draw structs input */
//        glBindBufferRange(GL_SHADER_STORAGE_BUFFER, 1, worldMap.indirectDrawBuffer, (long) currentDynamicBufferIndex * GameProperties.MAX_ACTIVE_CHUNKS * 2 * Integer.BYTES, (long) GameProperties.MAX_ACTIVE_CHUNKS * 2 * Integer.BYTES);
//        /* Bind buffer to output MDI draw structs for _all_ non-occluded in-frustum chunks */
//        glBindBufferRange(GL_SHADER_STORAGE_BUFFER, 2, indirectDrawCulledBuffer, (long) currentDynamicBufferIndex * GameProperties.MAX_ACTIVE_CHUNKS * 5 * Integer.BYTES, (long) GameProperties.MAX_ACTIVE_CHUNKS * 5 * Integer.BYTES);
//        if (useTemporalCoherenceOcclusionCulling) {
//            /*
//             * If we use temporal coherence occlusion culling, we split the buffers into two distinct regions:
//             * One for all non-occluded in-frustum chunks, and one for newly disoccluded in-frustum chunks.
//             */
//            /* Bind atomic counter for counting newly disoccluded in-frustum chunks */
//            glBindBufferRange(GL_ATOMIC_COUNTER_BUFFER, 1, atomicCounterBuffer, (long) (GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS + currentDynamicBufferIndex) * Integer.BYTES, Integer.BYTES);
//            clearAtomicCounter(1);
//            /* Bind buffer for the visibility flags of last frame's chunks */
//            glBindBufferRange(GL_SHADER_STORAGE_BUFFER, 3, visibilityFlagsBuffer, (long) lastFrameDynamicBufferIndex() * GameProperties.MAX_ACTIVE_CHUNKS * Integer.BYTES, (long) GameProperties.MAX_ACTIVE_CHUNKS * Integer.BYTES);
//            /* Bind buffer to output MDI draw structs for newly disoccluded in-frustum chunks */
//            glBindBufferRange(GL_SHADER_STORAGE_BUFFER, 4, indirectDrawCulledBuffer, (long) (GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS + currentDynamicBufferIndex) * GameProperties.MAX_ACTIVE_CHUNKS * 5 * Integer.BYTES, (long) GameProperties.MAX_ACTIVE_CHUNKS * 5 * Integer.BYTES);
//        }
//    }
//
//    /**
//     * Zero-out the given section {0, 1} of the currently bound atomic counter buffer.
//     */
//    private void clearAtomicCounter(int section) {
//        try (MemoryStack stack = stackPush()) {
//            long atomicOffset = ((long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * section + currentDynamicBufferIndex) * Integer.BYTES;
//            long atomicSize = Integer.BYTES;
//            if (useClearBuffer) {
//                glClearBufferSubData(GL_ATOMIC_COUNTER_BUFFER, GL_R32UI, atomicOffset, atomicSize, GL_RED_INTEGER, GL_UNSIGNED_INT, stack.ints(0));
//            } else {
//                glBufferSubData(GL_ATOMIC_COUNTER_BUFFER, atomicOffset, stack.callocInt(1));
//            }
//        }
//    }
//
//    /**
//     * After the visibility SSBO was filled by {@link #drawBoundingBoxesOfInFrustumChunks()}, we append
//     * MDI draw commands from an input SSBO to an output SSBO for visible chunks.
//     */
//    private void collectDrawCommands() {
//        preCollectDrawCommandsState();
////        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
//        /*
//         * Start drawing from the correct array buffer offset/region. We don't use the 'firstVertex' in the
//         * glDrawArrays() call but offset the vertex attrib pointer, because we want gl_VertexID to start
//         * from zero in the shader later.
//         */
//        long vertexByteOffset = (long) currentDynamicBufferIndex * GameProperties.MAX_ACTIVE_CHUNKS * 4 * Integer.BYTES;
//        glVertexAttribIPointer(0, 4, GL_UNSIGNED_INT, 0, vertexByteOffset);
//        glDrawArrays(GL_POINTS, 0, numChunksInFrustum);
//        postCollectDrawCommandsState();
//    }
//
//    /**
//     * Reset of critical global GL state, that we cannot assume the next draw call to reset itself,
//     * after {@link #collectDrawCommands()}.
//     */
//    private void postCollectDrawCommandsState() {
//        glDisable(GL_RASTERIZER_DISCARD);
//    }
//
//    /**
//     * Insert a fence sync to be notified when the GPU has finished rendering from the indirect draw
//     * buffer.
//     * <p>
//     * We will wait for this to happen in {@link #updateIndirectBufferWithInFrustumChunks()} when
//     * {link #useBufferStorage}.
//     */
//    private void insertFenceSync() {
//        if (dynamicBufferUpdateFences[currentDynamicBufferIndex] != 0L) {
//            glDeleteSync(dynamicBufferUpdateFences[currentDynamicBufferIndex]);
//        }
//        dynamicBufferUpdateFences[currentDynamicBufferIndex] = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
//        currentDynamicBufferIndex = (currentDynamicBufferIndex + 1) % GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS;
//    }
//
//    /**
//     * Update the two main input buffers for rendering chunks' bounding boxes and collecting MDI draw
//     * structs from visible/non-occluded chunks:
//     * <ul>
//     * <li>an array buffer containing the position and size of in-frustum chunks (this will be used with
//     * a simple glDrawArrays() to draw points which will be expanded by a geometry shader to bounding
//     * boxes)
//     * <li>the face offsets and counts for all such in-frustum chunks
//     * </ul>
//     */
//    private void updateBoundingBoxesInputBuffersForInFrustumChunks() {
//        long faceOffsetsAndCounts, bb;
//        long faceOffsetsAndCountsPos, bbPos;
//        if (useBufferStorage) {
//            faceOffsetsAndCounts = worldMap.indirectDrawBufferAddr;
//            faceOffsetsAndCountsPos = currentDynamicBufferIndex * GameProperties.MAX_ACTIVE_CHUNKS * 2 * Integer.BYTES;
//            bb = boundingBoxesVertexBufferObjectAddr;
//            bbPos = currentDynamicBufferIndex * GameProperties.MAX_ACTIVE_CHUNKS * 4 * Integer.BYTES;
//        } else {
//            faceOffsetsAndCounts = nmemAlloc(GameProperties.MAX_ACTIVE_CHUNKS * 5 * Integer.BYTES);
//            faceOffsetsAndCountsPos = 0L;
//            bb = nmemAlloc(GameProperties.MAX_ACTIVE_CHUNKS * 4 * Integer.BYTES);
//            bbPos = 0L;
//        }
//        int numVisible = 0;
//        for (int i = 0; i < worldMap.getChunkHolder().count(); i++) {
//            Chunk c = worldMap.getChunkHolder().getAllChunks().get(i);
//            boolean chunkMustBeDrawn = playerInsideChunk(c);
//            if (!c.ready || renderedWorld.chunkNotInFrustum(c) && !chunkMustBeDrawn) {
//                continue;
//            }
//            faceOffsetsAndCountsPos += putChunkFaceOffsetAndCount(c, faceOffsetsAndCounts + faceOffsetsAndCountsPos);
//            memPutInt(bb + bbPos, c.cx << GameProperties.CHUNK_SIZE_SHIFT);
//            memPutInt(bb + bbPos + Integer.BYTES, c.cz << GameProperties.CHUNK_SIZE_SHIFT);
//            memPutInt(bb + bbPos + 2 * Integer.BYTES, c.getMinY() | c.getMaxY() << 16);
//            memPutInt(bb + bbPos + 3 * Integer.BYTES, c.index | (chunkMustBeDrawn ? 1 << 31 : 0));
//            bbPos += 4 * Integer.BYTES;
//            numVisible++;
//        }
//        long faceOffsetsAndCountsOffset = (long) currentDynamicBufferIndex * GameProperties.MAX_ACTIVE_CHUNKS * 2 * Integer.BYTES;
//        long faceOffsetsAndCountsSize = (long) numVisible * 2 * Integer.BYTES;
//        long boundingBoxesOffset = (long) currentDynamicBufferIndex * GameProperties.MAX_ACTIVE_CHUNKS * 4 * Integer.BYTES;
//        long boundingBoxesSize = (long) numVisible * 4 * Integer.BYTES;
//        if (useBufferStorage) {
//            glBindBuffer(GL_DRAW_INDIRECT_BUFFER, worldMap.indirectDrawBuffer);
//            glBindBuffer(GL_ARRAY_BUFFER, boundingBoxesVertexBufferObject);
//            glFlushMappedBufferRange(GL_DRAW_INDIRECT_BUFFER, faceOffsetsAndCountsOffset, faceOffsetsAndCountsSize);
//            glFlushMappedBufferRange(GL_ARRAY_BUFFER, boundingBoxesOffset, boundingBoxesSize);
//        } else {
//            glBindBuffer(GL_DRAW_INDIRECT_BUFFER, worldMap.indirectDrawBuffer);
//            glBindBuffer(GL_ARRAY_BUFFER, boundingBoxesVertexBufferObject);
//            nglBufferSubData(GL_DRAW_INDIRECT_BUFFER, faceOffsetsAndCountsOffset, faceOffsetsAndCountsPos, faceOffsetsAndCounts);
//            nglBufferSubData(GL_ARRAY_BUFFER, boundingBoxesOffset, bbPos, bb);
//            nmemFree(faceOffsetsAndCounts);
//            nmemFree(bb);
//        }
//        numChunksInFrustum = numVisible;
//    }
//
//    /**
//     * Write face offset and count to later build an MDI glMultiDrawElementsIndirect struct (to draw the
//     * given chunk).
//     */
//    private int putChunkFaceOffsetAndCount(Chunk c, long faceOffsetsAndCounts) {
//        memPutInt(faceOffsetsAndCounts, c.r.off);
//        memPutInt(faceOffsetsAndCounts + Integer.BYTES, c.r.len);
//        return Integer.BYTES << 1;
//    }
//
//    /**
//     * Fill the current region of the dynamic indirect draw buffer with draw commands for in-frustum
//     * chunks and return the number of such chunks.
//     */
//    private int updateIndirectBufferWithInFrustumChunks() {
//        int numChunks = 0;
//        long indirect, indirectPos;
//        long offset = (long) currentDynamicBufferIndex * GameProperties.MAX_ACTIVE_CHUNKS * 5 * Integer.BYTES;
//        if (useBufferStorage) {
//            indirect = worldMap.indirectDrawBufferAddr;
//            indirectPos = offset;
//        } else {
//            indirect = nmemAlloc(5 * Integer.BYTES * worldMap.getChunkHolder().count());
//            indirectPos = 0L;
//        }
//        for (int i = 0; i < worldMap.getChunkHolder().count(); i++) {
//            Chunk c = worldMap.getChunkHolder().getAllChunks().get(i);
//            if (!c.ready || renderedWorld.chunkNotInFrustum(c)) {
//                continue;
//            }
//            memPutInt(indirect + indirectPos, c.r.len * PerFaceBuffers.indicesPerFace);
//            memPutInt(indirect + indirectPos + Integer.BYTES, 1);
//            memPutInt(indirect + indirectPos + Integer.BYTES * 2, c.r.off * PerFaceBuffers.indicesPerFace);
//            memPutInt(indirect + indirectPos + Integer.BYTES * 3, c.r.off * PerFaceBuffers.verticesPerFace);
//            memPutInt(indirect + indirectPos + Integer.BYTES * 4, c.index);
//            indirectPos += Integer.BYTES * 5;
//            numChunks++;
//        }
//        if (useBufferStorage) {
//            glBindBuffer(GL_DRAW_INDIRECT_BUFFER, worldMap.indirectDrawBuffer);
//            glFlushMappedBufferRange(GL_DRAW_INDIRECT_BUFFER, offset, (long) numChunks * 5 * Integer.BYTES);
//        } else {
//            glBindBuffer(GL_DRAW_INDIRECT_BUFFER, worldMap.indirectDrawBuffer);
//            nglBufferSubData(GL_DRAW_INDIRECT_BUFFER, offset, indirectPos, indirect);
//            nmemFree(indirect);
//        }
//        return numChunks;
//    }
//
//    /**
//     * Wait for the fence sync before we can modify the current mapped buffer storage region.
//     * <p>
//     * We inserted a fence sync in {@link #runUpdateAndRenderLoop()} when {link #useBufferStorage},
//     * because with client-mapped memory it's our own responsibility to not touch that memory until the
//     * GPU has finished using it.
//     */
//    private void waitForFenceSync() {
//        if (dynamicBufferUpdateFences[currentDynamicBufferIndex] == 0L) {
//            return;
//        }
//        /* wait for fence sync before we can modify the mapped buffer range */
//        int waitReturn = glClientWaitSync(dynamicBufferUpdateFences[currentDynamicBufferIndex], 0, 0L);
//        while (waitReturn != GL_ALREADY_SIGNALED && waitReturn != GL_CONDITION_SATISFIED) {
//            if (GameProperties.DEBUG) {
//                System.out.println("Need to wait for fence sync!");
//            }
//            waitReturn = glClientWaitSync(dynamicBufferUpdateFences[currentDynamicBufferIndex], GL_SYNC_FLUSH_COMMANDS_BIT, 1L);
//        }
//    }
//
//    /**
//     * Setup GL state prior to drawing the chunks.
//     */
//    private void preDrawChunksState() {
//        if (wireframe) {
//            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
//        } else {
//            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
//        }
//        if (drawPointsWithGS) {
//            glDisable(GL_CULL_FACE);
//        } else {
//            glEnable(GL_CULL_FACE);
//        }
//        glEnable(GL_DEPTH_TEST);
////        glDisable(GL_BLEND);
//        glEnable(GL_BLEND);
//        glActiveTexture(GL_TEXTURE0);
//        glBindTexture(GL_TEXTURE_BUFFER, materialsTexture);
//        if (!useMultiDrawIndirect) {
//            glActiveTexture(GL_TEXTURE1);
//            glBindTexture(GL_TEXTURE_BUFFER, PerFaceBuffers.chunkInfoTexture);
//        }
//        glBindVertexArray(PerFaceBuffers.chunksVao);
//        glUseProgram(chunksProgram);
//        /*
//         * Bind the UBO holding camera matrices for drawing the chunks.
//         */
//        int uboSize = roundUpToNextMultiple(chunksProgramUboSize, uniformBufferOffsetAlignment);
//        glBindBufferRange(GL_UNIFORM_BUFFER, chunksProgramUboBlockIndex, chunksProgramUbo, (long) currentDynamicBufferIndex * uboSize, uboSize);
//        if (canSourceIndirectDrawCallCountFromBuffer) {
//            /*
//             * Bind the atomic counter buffer to the indirect parameter count binding point, to source the
//             * drawcall count from that buffer.
//             */
//            glBindBuffer(GL_PARAMETER_BUFFER_ARB, atomicCounterBuffer);
//        } else {
//            /*
//             * Bind the atomic counter buffer to the atomic counter buffer binding point. We will use
//             * glGetBufferSubData() to actually read-back the value of the counter from the buffer.
//             */
//            glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, atomicCounterBuffer);
//        }
//        /*
//         * Bind the indirect buffer containing the final MDI draw structs for all chunks that are visible.
//         */
//        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, indirectDrawCulledBuffer);
//    }
//
//    /**
//     * Setup GL state prior to drawing the chunks with an MDI call where the MDI structs are generated
//     * by the CPU.
//     */
//    private void preDrawChunksIndirectCpuGeneratedState() {
//        preDrawChunksState();
//        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, worldMap.indirectDrawBuffer);
//        int uboSize = roundUpToNextMultiple(chunksProgramUboSize, uniformBufferOffsetAlignment);
//        glBindBufferRange(GL_UNIFORM_BUFFER, chunksProgramUboBlockIndex, chunksProgramUbo, (long) currentDynamicBufferIndex * uboSize, uboSize);
//    }
//
//    /**
//     * Draw the voxels via the non-MDI render path with glMultiDrawElementsBaseVertex().
//     */
//    private void drawChunksWithMultiDrawElementsBaseVertex() {
//        if (PerFaceBuffers.chunksVao == 0) {
//            return;
//        }
//        preDrawChunksState();
//        try (MemoryStack stack = stackPush()) {
//            List<Chunk> allChunk = renderedWorld.getAllChunk();
//            int size = allChunk.size();
//            PointerBuffer indices = stack.mallocPointer(size);
//            IntBuffer count = stack.mallocInt(size);
//            IntBuffer basevertex = stack.mallocInt(size);
//            long total = 0;
//            for (Chunk c : allChunk) {
////                if(Math.random()>0.1f){continue;}
//                if (!c.ready || renderedWorld.chunkNotInFrustum(c)) {
//                    continue;
//                }
//                indices.put((long) Short.BYTES * c.r.off * PerFaceBuffers.indicesPerFace);
//                count.put(c.r.len * PerFaceBuffers.indicesPerFace);
//                basevertex.put(c.r.off * PerFaceBuffers.verticesPerFace);
//                total += Short.BYTES * c.r.off * PerFaceBuffers.indicesPerFace;
//            }
//            indices.flip();
//            count.flip();
//            basevertex.flip();
//            updateChunksProgramUbo();
//            if(DEBUG2){
//                System.out.println("draw count: "+total);
//            }
//            profile("glMultiDrawElementsBaseVertex",()->{   glMultiDrawElementsBaseVertex(GL_TRIANGLE_STRIP, count, GL_UNSIGNED_SHORT, indices, basevertex);});
//        }
//    }
//
//    /**
//     * Draw all chunks via a single MDI glMultiDrawElementsIndirect() call with MDI structs written by
//     * the CPU.
//     */
//    private void drawChunksWithMultiDrawElementsIndirectCpuGenerated(int numChunks) {
//        if (PerFaceBuffers.chunksVao == 0) {
//            return;
//        }
//        updateChunksProgramUbo();
//        preDrawChunksIndirectCpuGeneratedState();
//        glMultiDrawElementsIndirect(drawPointsWithGS ? GL_POINTS : GL_TRIANGLE_STRIP, GL_UNSIGNED_SHORT, (long) GameProperties.MAX_ACTIVE_CHUNKS * currentDynamicBufferIndex * 5 * Integer.BYTES, numChunks, 0);
//    }
//
//    /**
//     * Draw all chunks via a single MDI glMultiDrawElementsIndirectCount() call (or
//     * glMultiDrawElementsIndirect()) with MDI structs written by the GPU and sourcing the draw count
//     * from a GL buffer.
//     * <p>
//     * This takes in the dynamic buffer index we want to use for drawing, to be able to draw the last
//     * frame's chunks again for temporal coherence occlusion culling. Additionally, it allows to draw
//     * only the newly disoccluded chunks
//     *
//     * @param dynamicBufferIndex determines whether we want to draw last frame's or this frame's dynamic
//     *                           data
//     * @param onlyDisoccluded    determines whether we want to draw _all_ non-occluded in-frustum chunks
//     *                           (<code>false</code>), or only newly disoccluded chunks
//     *                           (<code>true</code>)
//     */
//    private void drawChunksWithMultiDrawElementsIndirectGpuGenerated(int dynamicBufferIndex,
//                                                                     boolean onlyDisoccluded) {
//        if (PerFaceBuffers.chunksVao == 0) {
//            return;
//        }
//        updateChunksProgramUbo();
//        preDrawChunksState();
//        /*
//         * Add barrier for shader writes to buffer objects that we use as GL_DRAW_INDIRECT_BUFFER (and
//         * GL_PARAMETER_BUFFER_ARB) which the GL_COMMAND_BARRIER_BIT covers, done by the collection step.
//         */
//        glMemoryBarrier(GL_COMMAND_BARRIER_BIT);
//        /*
//         * If we use temporal coherence occlusion culling, we have two sections in each buffer. One for all
//         * non-occluded in-frustum chunks, and one for newly disoccluded chunks.
//         */
//        if (canSourceIndirectDrawCallCountFromBuffer) {
//            /*
//             * We use the atomic counter buffer bound to GL_PARAMETER_BUFFER_ARB to source the drawcall count.
//             */
//            glMultiDrawElementsIndirectCountARB(drawPointsWithGS ? GL_POINTS : GL_TRIANGLE_STRIP, GL_UNSIGNED_SHORT, ((long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * (onlyDisoccluded ? 1 : 0) + dynamicBufferIndex) * GameProperties.MAX_ACTIVE_CHUNKS * 5 * Integer.BYTES, ((long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * (onlyDisoccluded ? 1 : 0) + dynamicBufferIndex) * Integer.BYTES, numChunksInFrustum, 0);
//        } else {
//            /*
//             * TODO: Do we _really_ want to support this path? if ARB_indirect_parameters is not available, we
//             * MUST CPU-readback the atomic counter value to provide to glMultiDrawElementsIndirect(), which
//             * stalls everything.
//             */
//            try (MemoryStack stack = stackPush()) {
//                IntBuffer counter = stack.mallocInt(1);
//                glGetBufferSubData(GL_ATOMIC_COUNTER_BUFFER, ((long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * (onlyDisoccluded ? 1 : 0) + currentDynamicBufferIndex) * Integer.BYTES, counter);
//                glMultiDrawElementsIndirect(drawPointsWithGS ? GL_POINTS : GL_TRIANGLE_STRIP, GL_UNSIGNED_SHORT, ((long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * (onlyDisoccluded ? 1 : 0) + dynamicBufferIndex) * GameProperties.MAX_ACTIVE_CHUNKS * 5 * Integer.BYTES, counter.get(0), 0);
//            }
//        }
//    }
//
//    /**
//     * Update the current region of the UBO for the voxels program.
//     */
//    private void updateChunksProgramUbo() {
//        int size = roundUpToNextMultiple(chunksProgramUboSize, uniformBufferOffsetAlignment);
//        try (MemoryStack stack = stackPush()) {
//            long ubo, uboPos;
//            if (useBufferStorage) {
//                ubo = chunksProgramUboAddr;
//                uboPos = currentDynamicBufferIndex * size;
//            } else {
//                ubo = stack.nmalloc(size);
//                uboPos = 0L;
//            }
//            mvpMat.getToAddress(ubo + uboPos);
//            uboPos += 16 * Float.BYTES;
//            mvpMat.getRow(3, tmpv4f).getToAddress(ubo + uboPos);
//            uboPos += 4 * Float.BYTES;
//            memPutInt(ubo + uboPos, (int) floor(player.getPosition().x));
//            memPutInt(ubo + uboPos + Integer.BYTES, (int) floor(player.getPosition().y));
//            memPutInt(ubo + uboPos + Integer.BYTES * 2, (int) floor(player.getPosition().z));
//            uboPos += 3 * Float.BYTES;
//            glBindBuffer(GL_UNIFORM_BUFFER, chunksProgramUbo);
//            if (useBufferStorage) {
//                glFlushMappedBufferRange(GL_UNIFORM_BUFFER, (long) currentDynamicBufferIndex * size, size);
//            } else {
//                nglBufferSubData(GL_UNIFORM_BUFFER, (long) currentDynamicBufferIndex * size, uboPos, ubo);
//            }
//        }
//    }
//
//    /**
//     * Update the uniform buffer object for the bounding boxes program.
//     */
//    private void updateBoundingBoxesProgramUbo() {
//        int size = roundUpToNextMultiple(boundingBoxesProgramUboSize, uniformBufferOffsetAlignment);
//        try (MemoryStack stack = stackPush()) {
//            long ubo, uboPos;
//            if (useBufferStorage) {
//                ubo = boundingBoxesProgramUboAddr;
//                uboPos = currentDynamicBufferIndex * size;
//            } else {
//                ubo = stack.nmalloc(size);
//                uboPos = 0L;
//            }
//            mvpMat.getToAddress(ubo + uboPos);
//            uboPos += 16 * Float.BYTES;
//            memPutInt(ubo + uboPos, (int) floor(player.getPosition().x));
//            memPutInt(ubo + uboPos + Integer.BYTES, (int) floor(player.getPosition().y));
//            memPutInt(ubo + uboPos + Integer.BYTES * 2, (int) floor(player.getPosition().z));
//            uboPos += 3 * Integer.BYTES;
//            glBindBuffer(GL_UNIFORM_BUFFER, boundingBoxesProgramUbo);
//            if (useBufferStorage) {
//                glFlushMappedBufferRange(GL_UNIFORM_BUFFER, (long) currentDynamicBufferIndex * size, size);
//            } else {
//                nglBufferSubData(GL_UNIFORM_BUFFER, (long) currentDynamicBufferIndex * size, uboPos, ubo);
//            }
//        }
//    }
//
//    private static int rgb(int r, int g, int b) {
//        return r | g << 8 | b << 16 | 0xFF << 24;
//    }
//
//    /**
//     * Loop in the main thread to only process OS/window event messages.
//     * <p>
//     * See {@link #registerWindowCallbacks()} for all callbacks that may fire due to events.
//     */
//    private void runWndProcLoop() {
//        glfwShowWindow(window);
//        while (!glfwWindowShouldClose(window)) {
//            glfwWaitEvents();
//            if (updateWindowTitle) {
//                glfwSetWindowTitle(window, windowStatsString);
//                updateWindowTitle = false;
//            }
//        }
//    }
//
//    /**
//     * Compute last frame's dynamic buffer index.
//     * <p>
//     * This will be used for temporal coherence occlusion culling to draw last frame's visible chunks.
//     */
//    private int lastFrameDynamicBufferIndex() {
//        return (currentDynamicBufferIndex + GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS - 1) % GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS;
//    }
//
//    /**
//     * Run the "update and render" loop in a separate thread.
//     * <p>
//     * This is to decouple rendering from possibly long-blocking polling of OS/window messages (via
//     * {@link GLFW#glfwPollEvents()}).
//     */
//    private void runUpdateAndRenderLoop() {
//        glfwMakeContextCurrent(window);
//        GL.setCapabilities(caps);
//        long lastTime = System.nanoTime();
//        while (!glfwWindowShouldClose(window)) {
//            /*
//             * Compute time difference between this and last frame.
//             */
//            long thisTime = System.nanoTime();
//            float dt = (thisTime - lastTime) * 1E-9f;
//            lastTime = thisTime;
//            if (!GameProperties.FULLSCREEN) {
//                /*
//                 * Update stats in window title if we run in windowed mode.
//                 */
//                updateStatsInWindowTitle(dt);
//            }
//            /*
//             * Execute any runnables that have accumulated in the render queue. These are GL calls for
//             * created/updated chunks.
//             */
//            drainRunnables();
//            /*
//             * Bind FBO to which we will render.
//             */
//            glBindFramebuffer(GL_FRAMEBUFFER, fbo);
//            if (useBufferStorage) {
//                /*
//                 * If we use ARB_buffer_storage and with it persistently mapped buffers, we must explicitly sync
//                 * between updating those buffers and rendering from those buffers ourselves, because OpenGL won't
//                 * do that anymore (which is good!).
//                 */
//                waitForFenceSync();
//            }
//            // на маках это не работает, убрал, чтобы не мешался код
////            if (useOcclusionCulling) {
////                if (useTemporalCoherenceOcclusionCulling) {
////                    /*
////                     * Clear color and depth now, because we will draw last frame's chunks for priming depth and color
////                     * buffers.
////                     */
////                    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
////                    /*
////                     * Update player's position and matrices.
////                     */
////                    updatePlayerPositionAndMatrices(dt);
////                    /*
////                     * Create new in-view chunks and destroy out-of-view chunks.
////                     */
////                    while (createInRenderDistanceAndDestroyOutOfRenderDistanceChunks())
////                        ;
////                    // Determine the selected voxel in the center of the viewport.
////                    determineSelectedVoxel();
////                    /*
////                     * Draw the same chunks that were visible last frame by using last frame's dynamic buffer index, so
////                     * we use last frame's MDI draw structs. Newly disoccluded chunks that were not visible last frame
////                     * will be rendered with another draw call below.
////                     */
////                    drawChunksWithMultiDrawElementsIndirectGpuGenerated(lastFrameDynamicBufferIndex(), false);
////                    /*
////                     * Render bounding boxes of in-frustum chunks to set visibility flags via SSBO writes in the
////                     * fragment shader.
////                     */
////                    drawBoundingBoxesOfInFrustumChunks();
////                    /*
////                     * Collect MDI structs for in-frustum chunks marked visible by the bounding boxes draw.
////                     * Additionally, collect newly disoccluded chunks that were not visible last frame.
////                     */
////                    collectDrawCommands();
////                    /*
////                     * Now draw all newly disoccluded chunks which were not visible last frame for which we collected
////                     * the MDI structs in the collect call above.
////                     */
////                    drawChunksWithMultiDrawElementsIndirectGpuGenerated(currentDynamicBufferIndex, true);
////                } else {
////                    /*
////                     * If we don't want to use temporal coherence occlusion culling, we simply draw the chunks' bounding
////                     * boxes tested against the last frame's depth buffer using the last view matrices. This will lead
////                     * to visible popping of disoccluded chunks.
////                     */
////                    drawBoundingBoxesOfInFrustumChunks();
////                    /*
////                     * Clear color and depth buffers now after we've tested the bounding boxes against last frame's
////                     * depth buffer.
////                     */
////                    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
////                    /*
////                     * Update player's position and matrices.
////                     */
////                    updatePlayerPositionAndMatrices(dt);
////                    /*
////                     * Create new in-view chunks and destroy out-of-view chunks.
////                     */
////                    while (createInRenderDistanceAndDestroyOutOfRenderDistanceChunks())
////                        ;
////                    // Determine the selected voxel in the center of the viewport.
////                    determineSelectedVoxel();
////                    /*
////                     * Collect MDI structs for in-frustum chunks marked visible by the bounding boxes draw.
////                     */
////                    collectDrawCommands();
////                    /*
////                     * Draw all chunks for which we collected the MDI structs.
////                     */
////                    drawChunksWithMultiDrawElementsIndirectGpuGenerated(currentDynamicBufferIndex, false);
////                }
////            } else {
//            /*
//             * If we don't want to do any sort of occlusion culling, we clear color and depth buffers and update
//             * the player's position and matrices.
//             */
//            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//            /*
//             * Update player's position and matrices.
//             */
//            updatePlayerPositionAndMatrices(dt);
// /*
////                     * Create new in-view chunks and destroy out-of-view chunks.
////                     */
//            renderedWorld.createInRenderDistanceAndDestroyOutOfRenderDistanceChunks();
//            renderedWorld.rebuildMeshForUpdatedChunks();
//            determineSelectedVoxel();
////            drawBoundingBoxesOfInFrustumChunks();
//
//            /*
//             * Check if we support MDI.
//             */
////                if (useMultiDrawIndirect) {
////                    int numChunks = updateIndirectBufferWithInFrustumChunks();
////                    drawChunksWithMultiDrawElementsIndirectCpuGenerated(numChunks);
////                } else {
//            /*
//             * If not, we will just use multi-draw without indirect.
//             */
//            if (1 == 1)
//                profile("drawChunksWithMultiDrawElementsBaseVertex", this::drawChunksWithMultiDrawElementsBaseVertex);
//
////                }
////            }
//            /*
//             * Draw highlighting of selected voxel face.
//             */
//            profile("drawSelection", this::drawSelection);
//            profile("drawGUI", this::drawGUI);
//            RenderText.render(font, "X:" + round(player.getPosition().x) + " Y:" + round(player.getPosition().y) + " Z:" + round(player.getPosition().z), 0.3f, 0.3f);
////            collectDrawCommands();
//            virtualPlaneRender.draw(player.getPosition(), mvpMat);
//            /*
//             * Insert GPU fence sync that we will wait for a few frames later when we come back at updating the
//             * same dynamic buffer region.
//             */
//            if (useBufferStorage) {
//                insertFenceSync();
//            }
//            /*
//             * Blit FBO to the window.
//             */
//            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
//            glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_COLOR_BUFFER_BIT, GL_NEAREST);
//            glfwSwapBuffers(window);
//        }
//        executorService.shutdown();
//        try {
//            if (!executorService.awaitTermination(2000L, TimeUnit.MILLISECONDS)) {
//                throw new AssertionError();
//            }
//        } catch (Exception e) {
//            throw new AssertionError();
//        }
//        drainRunnables();
//        GL.setCapabilities(null);
//    }
//
//    private int statsFrames;
//    private float statsTotalFramesTime;
//    private volatile boolean updateWindowTitle;
//    private String windowStatsString;
//
//    /**
//     * When in windowed mode, this method will be called to update certain statistics that are shown in the window
//     * title.
//     */
//    private void updateStatsInWindowTitle(float dt) {
//        if (statsTotalFramesTime >= 0.5f) {
//            int px = (int) floor(player.getPosition().x);
//            int py = (int) floor(player.getPosition().y);
//            int pz = (int) floor(player.getPosition().z);
//            windowStatsString = statsFrames * 2 + " FPS, " + GameProperties.INT_FORMATTER.format(worldMap.getChunkHolder().count()) + " act. chunks, " + GameProperties.INT_FORMATTER.format(numChunksInFrustum) + " chunks in frustum, GPU mem. " + GameProperties.INT_FORMATTER.format(worldMap.getChunkHolder().computePerFaceBufferObjectSize() / 1024 / 1024) + " MB @ " + px + " , " + py + " , " + pz;
//            statsFrames = 0;
//            statsTotalFramesTime = 0f;
//            updateWindowTitle = true;
//            glfwPostEmptyEvent();
//        }
//        statsFrames++;
//        statsTotalFramesTime += dt;
//    }
//
//    /**
//     * Process all update/render thread tasks in the {link #updateAndRenderRunnables} queue.
//     */
//    private void drainRunnables() {
//        Iterator<DelayedRunnable> it = updateAndRenderRunnables.iterator();
//        while (it.hasNext()) {
//            DelayedRunnable dr = it.next();
//            /* Check if we want to delay this runnable */
//            if (dr.delay > 0) {
//                if (GameProperties.DEBUG) {
//                    System.out.println("Delaying runnable [" + dr.name + "] for " + dr.delay + " frames");
//                }
//                dr.delay--;
//                continue;
//            }
//            try {
//                /* Remove from queue and execute */
//                it.remove();
//                dr.runnable.call();
//            } catch (Exception e) {
//                throw new AssertionError(e);
//            }
//        }
//    }
//
//    /**
//     * Configure OpenGL state for drawing the selected voxel face.
//     */
//    private void preDrawSelectionState() {
//        glDisable(GL_CULL_FACE);
//        glEnable(GL_DEPTH_TEST);
//        glEnable(GL_BLEND);
//        glEnable(GL_POLYGON_OFFSET_FILL);
//        glPolygonOffset(useInverseDepth ? 1 : -1, useInverseDepth ? 1 : -1);
//        glBindVertexArray(nullVao);
//        glUseProgram(selectionProgram);
//    }
//
//    /**
//     * Update the current region of the UBO for the drawing the selection quad.
//     */
//    private void updateSelectionProgramUbo(Matrix4f mvp, float r, float g, float b) {
//        /* Round up to the next multiple of the UBO alignment */
//        int size = roundUpToNextMultiple(selectionProgramUboSize, uniformBufferOffsetAlignment);
//        try (MemoryStack stack = stackPush()) {
//            long ubo, uboPos;
//            if (useBufferStorage) {
//                ubo = selectionProgramUboAddr;
//                uboPos = currentDynamicBufferIndex * size;
//            } else {
//                ubo = stack.nmalloc(size);
//                uboPos = 0L;
//            }
//            mvp.getToAddress(ubo + uboPos);
//            uboPos += 16 * Float.BYTES;
//            glBindBufferRange(GL_UNIFORM_BUFFER, selectionProgramUboBlockIndex, selectionProgramUbo, (long) currentDynamicBufferIndex * size, size);
//            if (useBufferStorage) {
//                glFlushMappedBufferRange(GL_UNIFORM_BUFFER, (long) currentDynamicBufferIndex * size, size);
//            } else {
//                nglBufferSubData(GL_UNIFORM_BUFFER, (long) currentDynamicBufferIndex * size, uboPos, ubo);
//            }
//        }
//    }
//
//    /**
//     * Update the current region of the UBO for the drawing the selection quad.
//     */
//    private void updateGUIProgramUbo(Matrix4f mvp) {
//        /* Round up to the next multiple of the UBO alignment */
//        int size = roundUpToNextMultiple(selectionProgramUboSize, uniformBufferOffsetAlignment);
//        try (MemoryStack stack = stackPush()) {
//            long ubo, uboPos;
////            if (useBufferStorage) {
////                ubo = selectionProgramUboAddr;
////                uboPos = currentDynamicBufferIndex * size;
////            } else {
//            ubo = stack.nmalloc(size);
//            uboPos = 0L;
////            }
//            mvp.getToAddress(ubo + uboPos);
//            uboPos += 16 * Float.BYTES;
//            //            System.out.println("color index"+selectedMaterial+" rgb:"+color);
//            selectedMaterialAsColor.getToAddress(ubo + uboPos);
//            uboPos += 16 * Float.BYTES;
//            glBindBufferRange(GL_UNIFORM_BUFFER, guiProgramUboBlockIndex, guiProgramUbo, (long) currentDynamicBufferIndex * size, size);
//            if (useBufferStorage) {
//                glFlushMappedBufferRange(GL_UNIFORM_BUFFER, (long) currentDynamicBufferIndex * size, size);
//            } else {
//                nglBufferSubData(GL_UNIFORM_BUFFER, (long) currentDynamicBufferIndex * size, uboPos, ubo);
//            }
//        }
//    }
//
//    /**
//     * Draw the highlighting of the selected voxel face.
//     */
//    private void drawSelection() {
//        if (!hasSelection) {
//            return;
//        }
//        preDrawSelectionState();
//        /* compute a player-relative position. The MVP matrix is already player-centered */
//        double dx = selectedVoxelPosition.x - floor(player.getPosition().x);
//        double dy = selectedVoxelPosition.y - floor(player.getPosition().y);
//        double dz = selectedVoxelPosition.z - floor(player.getPosition().z);
//        tmpMat.set(mvpMat).translate((float) dx, (float) dy, (float) dz);
//        /* translate and rotate based on face side */
//        if (sideOffset.x != 0) {
//            tmpMat.translate(sideOffset.x > 0 ? 1 : 0, 0, 1).mul3x3(0, 0, -1, 0, 1, 0, 1, 0, 0);
//        } else if (sideOffset.y != 0) {
//            tmpMat.translate(0, sideOffset.y > 0 ? 1 : 0, 1).mul3x3(1, 0, 0, 0, 0, -1, 0, 1, 0);
//        } else if (sideOffset.z != 0) {
//            tmpMat.translate(0, 0, sideOffset.z > 0 ? 1 : 0).mul3x3(1, 0, 0, 0, 1, 0, 0, 0, 1);
//        }
//        /* animate it a bit */
//        float s = (float) sin(System.currentTimeMillis() / 4E2);
//        tmpMat.translate(0.5f, 0.5f, 0f).scale(0.3f + 0.1f * s * s);
//        updateSelectionProgramUbo(tmpMat, 0.2f, 0.3f, 0.6f);
//        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
//        postDrawSelectionState();
//    }
//
//    /**
//     * Draw the highlighting of the selected voxel face.
//     */
//    private void drawGUI() {
//
//        glEnable(GL_BLEND);
//        glEnable(GL_POLYGON_OFFSET_FILL);
//        glBindVertexArray(nullVao);
//        glUseProgram(guiProgram);
//
////        GOOD
//        Matrix4f scale = new Matrix4f(
//                0.1f, 0f, 0f, 0f,
//                0f, 0.18f, 0f, 0f,
//                0f, 0f, -0f, 1f,
//                -0.9f, -0.83f, -0f, 1f);
////         Matrix for all screen
////        Matrix4f scale = new Matrix4f().ortho(-1f,1,-1,1f,-0f,-0.1f);
//
//
////        tmpMat.scale(1.2f, scale);
////        System.out.println(tmpMat);
//        updateGUIProgramUbo(scale);
//        glDrawArrays(GL_TRIANGLE_STRIP, 0, 8);
//
//        glDisable(GL_POLYGON_OFFSET_FILL);
//        glPolygonOffset(0, 0);
//    }
//
//    /**
//     * Reset of critical global GL state, that we cannot assume the next draw call to reset itself,
//     * after {@link #drawSelection()}.
//     */
//    private void postDrawSelectionState() {
//        glDisable(GL_POLYGON_OFFSET_FILL);
//        glPolygonOffset(0, 0);
//    }
//
//    /**
//     * Create a dedicated thread to process updates and perform rendering.
//     * <p>
//     * This is <em>only</em> for decoupling the render thread from potentially long-blocking
//     * {@link GLFW#glfwPollEvents()} calls (when e.g. many mouse move events occur).
//     * <p>
//     * Instead, whenever a OS/window event is received, it is enqueued into the
//     * {link #updateAndRenderRunnables} queue.
//     */
//    private Thread createAndStartUpdateAndRenderThread() {
//        Thread renderThread = new Thread(this::runUpdateAndRenderLoop);
//        renderThread.setName("Render Thread");
//        renderThread.setPriority(Thread.MAX_PRIORITY);
//        renderThread.start();
//        return renderThread;
//    }
//
//    /**
//     * Initialize and run the game/demo.
//     */
//    private void run() throws InterruptedException, IOException {
//        if (!glfwInit()) {
//            throw new IllegalStateException("Unable to initialize GLFW");
//        }
//
//        createWindow();
//        registerWindowCallbacks();
//        setWindowPosition();
//        queryFramebufferSizeForHiDPI();
//
//        initGLResources();
//
//        /* Временные затычки при рефакторинге**/
//        worldMap.player = player;
//        ChunkDistance.player = player;
//
//        /* Временные затычки при рефакторингеEND**/
//        // Создание чанков заранее
////        worldMap.getChunkHolder().getChunkByCoordinate(0, 0);
////        for (int x = 0; x < 5; x++) {
////            for (int y = 0; y < 5; y++) {
////                worldMap.getChunkHolder().ensureChunk(x, y);
////                worldMap.getChunkHolder().ensureChunk(-x, -y);
////                worldMap.getChunkHolder().ensureChunk(-x, y);
////                worldMap.getChunkHolder().ensureChunk(x, -y);
////            }
////        }
//
//
//        /* Run logic updates and rendering in a separate thread */
//        Thread updateAndRenderThread = createAndStartUpdateAndRenderThread();
//        /* Process OS/window event messages in this main thread */
//        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
//        /* Wait for the latch to signal that init render thread actions are done */
//        runWndProcLoop();
//        /*
//         * After the wnd loop exited (because the window was closed), wait for render thread to complete
//         * finalization.
//         */
//        updateAndRenderThread.join();
//        if (debugProc != null) {
//            debugProc.free();
//        }
//        glfwFreeCallbacks(window);
//        glfwDestroyWindow(window);
//        glfwTerminate();
//    }
//
//    private void initGLResources() throws IOException {
//        glfwMakeContextCurrent(window);
//
//        /* Determine, which additional OpenGL capabilities we have. */
//        determineOpenGLCapabilities();
//
//        /*
//         * Compute number of vertices per face and number of bytes per vertex. These depend on the features
//         * we are going to use.
//         */
//        PerFaceBuffers.verticesPerFace = drawPointsWithGS ? 1 : 4;
//        PerFaceBuffers.indicesPerFace = drawPointsWithGS ? 1 : 5;
//        PerFaceBuffers.voxelVertexSize = drawPointsWithGS ? 2 * Integer.BYTES : Integer.BYTES + Short.BYTES + (!useMultiDrawIndirect ? Integer.BYTES : 0);
//
//        if (GameProperties.DEBUG || GameProperties.GLDEBUG) {
//            installDebugCallback();
//        }
//        glfwSwapInterval(GameProperties.VSYNC ? 1 : 0);
//
//
//        //Загрузка шрифтов
//        List<String> letterSet = new ArrayList<>();
//        letterSet.add("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
//        letterSet.add("abcdefghijklmnopqrstuvwxyz");
//        letterSet.add("0123456789&'()*+-=.!\"#$%^§");
//        letterSet.add(",:;?@/<>|\\~~[]{}_~ ");
//        Font localFont = new Font("font/wCvnX.png", letterSet, 40, 54, 5, 8, 18, 9);
//        font = new FontRender(localFont);
//
//        grassTextureId = TextureLoader.loadTexture("texture/grass.png");
////        grassTextureId= TextureLoader.loadTexture("texture/grass.png");
//
//        virtualPlaneRender = new VirtualPlaneRender();
//        virtualPlaneRender.createProgram();
//        ;
//        virtualPlaneRender.createProgramUbo();
//
//        /* Configure OpenGL state and create all necessary resources */
//        configureGlobalGlState();
//        createSelectionProgram();
//        createSelectionProgramUbo();
//        createNullVao();
//        createMaterials();
//        worldMap.createMultiDrawIndirectBuffer();
//        PerFaceBuffers.createChunkInfoBuffers();
//        createChunksProgram();
//        createChunksProgramUbo();
////        if (canGenerateDrawCallsViaShader || true) {
//        createOcclusionCullingBufferObjects();
////        createBoundingBoxesProgram();
////        createCollectDrawCallsProgram();
////        createBoundingBoxesProgramUbo();
////        createBoundingBoxesVao();
////        }
//
//
//        createGUIVao();
//        createGUIProgram();
//        createGUIProgramUbo();
//
//
//        createFramebufferObject();
//
//
//        /* Make sure everything is ready before we show the window */
//        glFlush();
//        glfwMakeContextCurrent(NULL);
//        GL.setCapabilities(null);
//    }
//
//    public static void main(String[] args) throws Exception {
//        new VoxelGame2GL().run();
//    }
//}
