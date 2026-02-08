/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
package ru.mikst74.mikstcraft;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.mikst74.mikstcraft.dictionary.BlockTypeDictionary;
import ru.mikst74.mikstcraft.dictionary.BlockTypeInfo;
import ru.mikst74.mikstcraft.main.Starter;
import ru.mikst74.mikstcraft.model.NeighborCode;
import ru.mikst74.mikstcraft.model.Person;
import ru.mikst74.mikstcraft.model.camera.Camera;
import ru.mikst74.mikstcraft.model.chunk.VoxelField;
import ru.mikst74.mikstcraft.model.coo.WorldCoo;
import ru.mikst74.mikstcraft.model.font.Font;
import ru.mikst74.mikstcraft.model.time.GameTick;
import ru.mikst74.mikstcraft.render.ParticlesRenderer;
import ru.mikst74.mikstcraft.render.free3dobject.Free3DObjectRenderer;
import ru.mikst74.mikstcraft.render.shader.chunk.ChunkShaderProgram;
import ru.mikst74.mikstcraft.render.shader.font.FontShaderProgram;
import ru.mikst74.mikstcraft.render.shader.texturedquads.TexturedQuadsItem;
import ru.mikst74.mikstcraft.render.text.LetterQuadsRenderer;
import ru.mikst74.mikstcraft.render.text.TextAreaRenderer;
import ru.mikst74.mikstcraft.render.texturedquads.TexturedQuadsRenderer;
import ru.mikst74.mikstcraft.render.texturedquads.TexturedQuadsRendererFun;
import ru.mikst74.mikstcraft.render.voxel.VoxelRender;
import ru.mikst74.mikstcraft.settings.GameDynamicProperties;
import ru.mikst74.mikstcraft.settings.GameProperties;
import ru.mikst74.mikstcraft.texture.TextureInfo;
import ru.mikst74.mikstcraft.texture.TextureLoader;
import ru.mikst74.mikstcraft.util.DelayedRunnable;
import ru.mikst74.mikstcraft.world.chunk.ChunkDistance;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.util.*;

import static java.lang.Math.floor;
import static java.lang.Math.sin;
import static ru.mikst74.mikstcraft.model.NeighborCode.XM;
import static ru.mikst74.mikstcraft.model.NeighborCode.XP;
import static ru.mikst74.mikstcraft.render.opengl.ShaderCreator.createShader;
import static ru.mikst74.mikstcraft.settings.OpenGLProperties.*;
import static ru.mikst74.mikstcraft.util.BackgroundExecutor.updateAndRenderRunnables;
import static ru.mikst74.mikstcraft.util.math.ExtMath.roundUpToNextMultiple;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.ARBBufferStorage.GL_MAP_PERSISTENT_BIT;
import static org.lwjgl.opengl.ARBBufferStorage.glBufferStorage;
import static org.lwjgl.opengl.ARBClipControl.GL_ZERO_TO_ONE;
import static org.lwjgl.opengl.ARBClipControl.glClipControl;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_OUTPUT_SYNCHRONOUS_ARB;
import static org.lwjgl.opengl.ARBMultiDrawIndirect.glMultiDrawElementsIndirect;
import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.opengl.GLUtil.setupDebugMessageCallback;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memPutInt;

/**
 * A simple voxel game.
 *
 * @author Kai Burjack
 */
public class VoxelGame2GL {

    /*
     * Comparators to sort the chunks by whether they are in the view frustum and by distance to the
     * player.
     */


    /**
     * Tasks that must be run on the update/render thread, usually because they involve calling OpenGL
     * functions.
     */

//    public static long  window;
    public static int   width;
    public static int   height;
    //    private final Vector3f playerAcceleration = new Vector3f(0, -30.0f, 0);
//    private final Vector3f playerVelocity = new Vector3f();
//    private final Vector3d playerPosition = new Vector3d(0, 240, 0);
    private       float angx = 0f, angy = 3f, dangx, dangy, angz = 0;


    //    private final       Matrix4f       pMat         = new Matrix4f();
//    private final       Matrix4x3f     vMat         = new Matrix4x3f();
//    //    public static final Matrix4f       mvpMat       = new Matrix4f();
//    public static final Matrix4f       globalMvpMat = new Matrix4f();
//    private final       Matrix4f       imvpMat      = new Matrix4f();
    private final Matrix4f       tmpMat = new Matrix4f();
    private final Quaternionf    tmpq   = new Quaternionf();
    //    private             MaterialDictionary materials;
    private       Callback       debugProc;
    private       GLCapabilities caps;


    /* Other queried OpenGL state/configuration */

//    int grassTextureId;

    /**
     * Index identifying the current region of any dynamic buffer which we will use for updating and
     * drawing from.
     */

    private int fbo, colorRbo, depthRbo;

    private final boolean[] keydown = new boolean[GLFW_KEY_LAST + 1];
    private       int       mouseX, mouseY;
    private final       Vector3f tmpv3f = new Vector3f();
    public static final Vector4f tmpv4f = new Vector4f();
    private             boolean  jumping;


    /* Resources for drawing the chunks */
//    private int chunkInfoBufferObject;
    public static int  chunksProgram;
    public static int  materialsTexture;
    public static int  chunksProgramUboBlockIndex;
    public static int  chunksProgramUbo;
    public static long chunksProgramUboAddr;
    public static int  chunksProgramUboSize = 4 * (16 + 2 * 4);

    /* Resources for drawing chunks' bounding boxes to fill visibility buffer */
    private              int  visibilityFlagsBuffer;
    private              int  boundingBoxesVao;
    private              int  boundingBoxesProgram;
    private              int  boundingBoxesProgramUboBlockIndex;
    private              int  boundingBoxesProgramUbo;
    private              long boundingBoxesProgramUboAddr;
    private              int  boundingBoxesVertexBufferObject;
    private              long boundingBoxesVertexBufferObjectAddr;
    private static final int  boundingBoxesProgramUboSize = 4 * (16 + 4);

    /* GUI */
    private int guiVao;
    private int guiProgram;
    private int guiVertexBufferObject;
    private int guiProgramUbo;
    private int guiProgramUboBlockIndex;


    /* */

    /* Resources for collecting draw calls using the visibility buffer */
    private       int collectDrawCallsProgram;
    public static int atomicCounterBuffer;
    public static int indirectDrawCulledBuffer;

    /* Resources for drawing the "selection" quad */
    public static        int                      nullVao;
    private              int                      selectionProgram;
    private              int                      selectionProgramUboBlockIndex;
    private              int                      selectionProgramUbo;
    private              long                     selectionProgramUboAddr;
    private static final int                      selectionProgramUboSize = 4 * 16;
    private final        WorldCoo                 selectedVoxelPosition   = new WorldCoo();
    private              NeighborCode             sideOffset;
    private              boolean                  hasSelection;
    private              boolean                  firstCursorPos          = true;
    private              boolean                  fly                     = false;
    public static        boolean                  wireframe;
    private              boolean                  debugBoundingBoxes;
    private              BlockTypeInfo            selectedMaterial;
    private              FontShaderProgram        font;
    private              ParticlesRenderer        particlesRenderer;
    private              VoxelRender              voxelRender;
    private              TextAreaRenderer         textAreaRenderer;
    private              TexturedQuadsRendererFun texturedQuadsRendererFun;
    private              TexturedQuadsRenderer    centerCross;
    private              Free3DObjectRenderer     freePane;
    private              LetterQuadsRenderer      letterQuadsRenderer;
    private              int                      showGui                 = 0;
    private              boolean                  autoPlaceVoxel;
    public static        TextureInfo              grassTextureId;
    public               GameTick                 gameTickTst             = new GameTick();
    private Person player;
    private Camera camera;
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
//        mouseX         = (int) x;
//        mouseY         = (int) y;
//    }

    /**
     * Calls {findAndSetSelectedVoxel(float, float, float, float, float, float)} with the current
     * player position and orientation.
     */
//    private void determineSelectedVoxel() {
//        if (fly) {
//            hasSelection = false;
//            return;
//        }
//        Vector3f dir = tmpq.rotationX(angx).rotateY(angy).positiveZ(tmpv3f).negate();

//        freePane.getItems().get(0).getPos().x+= 0.01f;
//        freePane.getItems().get(0).getPos().y-= 0.01f;
//        freePane.getItems().get(0).getPos().z+= 0.01f;
//        freePane.getItems().get(0).getPos().x = (float) player.getPosition().x - 0.05f;
//        freePane.getItems().get(0).getPos().y = (float) player.getPosition().y - 0.1f;
//        freePane.getItems().get(0).getPos().z = (float) player.getPosition().z;
//        freePane.getItems().get(1).getPos().x = (float) player.getPosition().x + 0.05f;
//        freePane.getItems().get(1).getPos().y = (float) player.getPosition().y - 0.1f;
//        freePane.getItems().get(1).getPos().z = (float) player.getPosition().z;
//        freePane.getItems().get(2).getPos().x = dir.x * 10;
//        freePane.getItems().get(2).getPos().y = dir.y * 10;
//        freePane.getItems().get(2).getPos().z = dir.z * 10;


//        Vector3f dir = tmpq.rotationX(angx).rotateY(angy).positiveZ(tmpv3f).negate();
//        findAndSetSelectedVoxel((float) player.getPosition().x, (float) player.getPosition().y, (float) player.getPosition().z, dir.x, dir.y, dir.z);
//
//        hasSelection = RaytraceVoxelSearch.
//                traverseFromAI(player.getEyePosition(), camera.getDirection(), this::load, (wCoo, nc) -> {
//                    selectedVoxelPosition.assign(wCoo);
//                    sideOffset = nc;
//                });
//
//        hasSelection = RaytraceVoxelSearch.
//                findAndSetSelectedVoxelOld((float) player.getPosition().x, (float) player.getPosition().y, (float) player.getPosition().z, dir.x, dir.y, dir.z, this::load, (wCoo, nc) -> {
//                    selectedVoxelPosition.assign(wCoo);
//                    sideOffset = nc;
//                });
//        if (hasSelection && autoPlaceVoxel) {
//            placeAtSelectedVoxel();
//        }
//    }


    /**
     * Handle special keyboard keys before storing a key press/release state in the {@link #keydown}
     * array.
     */
    private void handleSpecialKeys(int key, int action) {
        if (key == GLFW_KEY_F && action == GLFW_PRESS) {
            fly = !fly;
            if (fly) {
                player.setCollisionDetector(null);
            } else {
//                player.setCollisionDetector(collisionDetector);
            }
        } else if (key == GLFW_KEY_L && action == GLFW_PRESS) {
            wireframe = !wireframe;
        } else if (key == GLFW_KEY_K && action == GLFW_PRESS) {
            debugBoundingBoxes = !debugBoundingBoxes;
        }
        if (key == GLFW_KEY_P && action == GLFW_PRESS) {
//            saver.save();
        }
        if (key >= GLFW_KEY_1 && key <= GLFW_KEY_9 && action == GLFW_PRESS) {
            setSelectedMaterial((byte) ((byte) key - 48));
        }
        if (key == GLFW_KEY_0 && action == GLFW_PRESS) {
            setSelectedMaterial((byte) 10);
        }
        if (key == GLFW_KEY_Z && action == GLFW_PRESS) {
            GameProperties.FOV_DEGREES = 12;
        }
        if (key == GLFW_KEY_Z && action == GLFW_RELEASE) {
            GameProperties.FOV_DEGREES = 72;
        }
        if (key == GLFW_KEY_N && action == GLFW_PRESS) {
            GameProperties.FAR -= 1;
        }
        if (key == GLFW_KEY_M && action == GLFW_PRESS) {
            GameProperties.FAR += 1;
        }

    }


    private void setSelectedMaterial(int m) {
        selectedMaterial = BlockTypeDictionary.getByNum(m);
    }


    /**
     * Return the voxel field value at the global position <code>(x, y, z)</code>.
     */
//    @Deprecated
//    private BlockTypeInfo load(int wx, int wy, int wz) {
//        return load(new WorldCoo(wx, wy, wz));
//    }
//
//    /**
//     * Return the voxel field value at the global position <code>(x, y, z)</code>.
//     */
//    private BlockTypeInfo load(WorldCoo coo) {
//        return worldMap.getVoxel(coo);
//    }


    /**
     * GLFW callback for mouse buttons.
     */
//    private void onMouseButton(long window, int button, int action, int mods) {
//        updateAndRenderRunnables.add(new DelayedRunnable(() -> {
//            if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS && hasSelection) {
////                autoPlaceVoxel = action == GLFW_PRESS;
//
//                placeAtSelectedVoxel();
//            } else if (button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS && hasSelection) {
//                removeSelectedVoxel();
//            }
//            return null;
//        }, "Mouse button event", 0));
//    }

//    private void removeSelectedVoxel() {
//        gameServer.sendMessage(createSetBlockMessage(selectedVoxelPosition, AIR_BLOCK));
//
////        worldMap.setVoxel(selectedVoxelPosition, AIR_BLOCK);
////        worldMap.store(selectedVoxelPosition.x, selectedVoxelPosition.y - 1, selectedVoxelPosition.z, (byte) 0);
//    }
//
//    private void placeAtSelectedVoxel() {
//        if (sideOffset != null) {
//            worldMap.setVoxel(selectedVoxelPosition.step(sideOffset), selectedMaterial);
//        }
//    }

//    /**
//     * Register all necessary GLFW callbacks.
//     */
//    private void registerWindowCallbacks() {
//        glfwSetFramebufferSizeCallback(window, this::onFramebufferSize);

    /// /        glfwSetKeyCallback(window, this::onKey);
//        glfwSetMouseButtonCallback(window, this::onMouseButton);
//        glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
//            int selectedMaterialId = selectedMaterial.getId();
//            if (yoffset < 0) {
//                selectedMaterialId++;
//            } else {
//                selectedMaterialId--;
//            }
//            if (selectedMaterialId < 1) {
//                selectedMaterialId = BlockTypeDictionary.count() - 1;
//            } else if (selectedMaterialId >= BlockTypeDictionary.count()) {
//                selectedMaterialId = 1;
//            }
//            setSelectedMaterial(selectedMaterialId);
//        });
//    }
//
    private void installDebugCallback() {
        debugProc = setupDebugMessageCallback();
        if (canUseSynchronousDebugCallback) {
            glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS_ARB);
        }
    }

    /**
     * Set global GL state that will not be changed afterwards.
     */
    private void configureGlobalGlState() {
        glClearColor(225 / 255f, 253 / 255f, 255 / 255f, 0f);
        glEnable(GL_PRIMITIVE_RESTART);
//        glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glPrimitiveRestartIndex(GameProperties.PRIMITIVE_RESTART_INDEX);
        if (useInverseDepth) {
            glClipControl(GL_LOWER_LEFT, GL_ZERO_TO_ONE);
            glDepthFunc(GL_GREATER);
            glClearDepth(0.0);
        } else {
            glDepthFunc(GL_LESS);
        }
    }


    /**
     * Create an empty VAO.
     */
    private void createNullVao() {
        nullVao = glGenVertexArrays();
    }


    /**
     * Create the shader program used to render the selection rectangle.
     */
    private void createSelectionProgram() throws IOException {
        int program = glCreateProgram();
        int vshader = createShader("org/lwjgl/demo/game2/voxelgame/shader/selection.vs.glsl", GL_VERTEX_SHADER, Collections.emptyMap());
        int fshader = createShader("org/lwjgl/demo/game2/voxelgame/shader/selection.fs.glsl", GL_FRAGMENT_SHADER, Collections.emptyMap());
        glAttachShader(program, vshader);
        glAttachShader(program, fshader);
        glLinkProgram(program);
        if (GameProperties.DEBUG || GameProperties.GLDEBUG) {
            int linked = glGetProgrami(program, GL_LINK_STATUS);
            String programLog = glGetProgramInfoLog(program);
            if (programLog.trim().length() > 0) {
                System.err.println(programLog);
            }
            if (linked == 0) {
                throw new AssertionError("Could not link program");
            }
        }
        glUseProgram(program);
        selectionProgramUboBlockIndex = glGetUniformBlockIndex(program, "Uniforms");
        glUseProgram(0);
        selectionProgram = program;
    }


    /**
     * Create a program used to draw chunks' bounding boxes by expanding points to cubes in a geometry
     * shader.
     */
    private void createGUIProgram() throws IOException {
        Map<String, String> defines = new HashMap<>();

        int program = glCreateProgram();
        int vshader = createShader("org/lwjgl/demo/game2/voxelgame/shader/gui.vs.glsl", GL_VERTEX_SHADER, defines);
//        int gshader = createShader("org/lwjgl/demo/mikstcraft/voxelgame/gui.gs.glsl", GL_GEOMETRY_SHADER, defines);
        int fshader = createShader("org/lwjgl/demo/game2/voxelgame/shader/gui.fs.glsl", GL_FRAGMENT_SHADER, defines);
        glAttachShader(program, vshader);
//        glAttachShader(program, gshader);
        glAttachShader(program, fshader);
        glLinkProgram(program);
        glDeleteShader(vshader);
//        glDeleteShader(gshader);
        glDeleteShader(fshader);
        if (GameProperties.DEBUG || GameProperties.GLDEBUG) {
            int linked = glGetProgrami(program, GL_LINK_STATUS);
            String programLog = glGetProgramInfoLog(program);
            if (programLog.trim().length() > 0) {
                System.err.println(programLog);
            }
            if (linked == 0) {
                throw new AssertionError("Could not link program");
            }
        }
        glUseProgram(program);
        guiProgramUboBlockIndex = glGetUniformBlockIndex(program, "Uniforms2");

        glUseProgram(0);
        guiProgram = program;
    }


    /**
     * Create the (multi-buffered) uniform buffer object to hold uniforms needed by the selection
     * program.
     */
    private void createSelectionProgramUbo() {
        selectionProgramUbo = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, selectionProgramUbo);
        int size = roundUpToNextMultiple(selectionProgramUboSize, uniformBufferOffsetAlignment);
        if (useBufferStorage) {
            glBufferStorage(GL_UNIFORM_BUFFER, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * size, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT);
            selectionProgramUboAddr = nglMapBufferRange(GL_UNIFORM_BUFFER, 0L, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * size, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_FLUSH_EXPLICIT_BIT);
        } else {
            glBufferData(GL_UNIFORM_BUFFER, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * size, GL_DYNAMIC_DRAW);
        }
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }

    /**
     * Create the (multi-buffered) uniform buffer object to hold uniforms needed by the selection
     * program.
     */
    private void createGUIProgramUbo() {
        guiProgramUbo = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, guiProgramUbo);
        int size = roundUpToNextMultiple(4 * (16 + 4 + 4), uniformBufferOffsetAlignment);
//        if (useBufferStorage) {
//            glBufferStorage(GL_UNIFORM_BUFFER, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * size, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT);
//            selectionProgramUboAddr = nglMapBufferRange(GL_UNIFORM_BUFFER, 0L, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * size, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_FLUSH_EXPLICIT_BIT);
//        } else {
        glBufferData(GL_UNIFORM_BUFFER, (long) GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS * size, GL_DYNAMIC_DRAW);
//        }
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }


    private void createMaterialsTexture() {
//        int materialsBufferObject = glGenBuffers();
//        try (MemoryStack stack = stackPush()) {
//            long materialsBuffer = stack.nmalloc(Integer.BYTES * materials.count());
//            for (int i = 0; i < materials.count(); i++) {
//                FaceMaterial mat = materials.get(i);
//                memPutInt(materialsBuffer + i * Integer.BYTES, mat == null ? 0 : mat.col);
//            }
//            glBindBuffer(GL_TEXTURE_BUFFER, materialsBufferObject);
//            if (useBufferStorage) {
//                nglBufferStorage(GL_TEXTURE_BUFFER, materials.count() * Integer.BYTES, materialsBuffer, 0);
//            } else {
//                nglBufferData(GL_TEXTURE_BUFFER, materials.count() * Integer.BYTES, materialsBuffer, GL_STATIC_DRAW);
//            }
//        }
//        glBindBuffer(GL_TEXTURE_BUFFER, 0);
//        materialsTexture = glGenTextures();
//        glBindTexture(GL_TEXTURE_BUFFER, materialsTexture);
//        glTexBuffer(GL_TEXTURE_BUFFER, GL_RGBA8, materialsBufferObject);
//        glBindTexture(GL_TEXTURE_BUFFER, 0);
    }

    private void handleKeyboardInput() {
        float factor = fly ? 16f : 5f;
        float dangz = 0.04f;
        float maxangz = 0.2f;

        player.getVelocity().x = 0.00f;
        player.getVelocity().y = -5.0f;
        player.getVelocity().z = 0f;
        if (fly) {
            player.getVelocity().y = 0.0f;
        }
        if (keydown[GLFW_KEY_LEFT_SHIFT]) {
            factor = fly ? 90f : 140f;
        }
        if (keydown[GLFW_KEY_W]) {
//            player.getVelocity().sub(vMat.positiveZ(tmpv3f).mul(factor, fly ? factor : 0, factor));
            player.getVelocity().x = factor;
        }
        if (keydown[GLFW_KEY_S]) {
            player.getVelocity().x = -factor;
//            player.getVelocity().add(vMat.positiveZ(tmpv3f).mul(factor, fly ? factor : 0, factor));
        }
        if (keydown[GLFW_KEY_A]) {
            angz -= dangz * 2;
            player.getVelocity().z = -factor;
//            player.getVelocity().sub(vMat.positiveX(tmpv3f).mul(factor, fly ? factor : 0, factor));
        }
        if (keydown[GLFW_KEY_D]) {
            player.getVelocity().z = factor;
            angz += dangz * 2;

//            player.getVelocity().add(vMat.positiveX(tmpv3f).mul(factor, fly ? factor : 0, factor));
        }
        if (keydown[GLFW_KEY_SPACE] && fly) {
            player.getVelocity().y = factor;

//            player.getVelocity().add(vMat.positiveY(tmpv3f).mul(fly ? factor : 1));
        }
        if (keydown[GLFW_KEY_LEFT_CONTROL] && fly) {
            player.getVelocity().y = -factor;
//            player.getVelocity().sub(vMat.positiveY(tmpv3f).mul(fly ? factor : 0));
        }
        if (!fly && keydown[GLFW_KEY_SPACE] && !jumping) {
            jumping = true;
            player.getVelocity().add(0, 13, 0);
        } else if (!keydown[GLFW_KEY_SPACE]) {
            jumping = false;
        }
        if (angz < 0) {
            angz += dangz;
        }
        if (angz > 0) {
            angz -= dangz;
        }
        if (angz > maxangz) {
            angz = maxangz;
        }
        if (angz < -maxangz) {
            angz = -maxangz;
        }
    }

    private void updatePlayerPositionAndMatrices(float dt) {
        handleKeyboardInput();
        angx += dangx * 0.002f;
        angy += dangy * 0.002f;
        dangx *= 0.0994f;
        dangy *= 0.0994f;

//        player.goForward();
//        player.goSide();

//        if (!fly) {
//            player.getVelocity().add(player.getAcceleration().mul(dt, tmpv3f));
////            handleCollisions(dt, player.getVelocity(), player.getPosition());
//        } else {
//        player.goForward();
//        }

//        vMat.rotation(tmpq.rotationX(angx).rotateY(angy).rotateLocalZ(angz));
//        vMat.translate((float) -(player.getPosition().x - floor(player.getPosition().x)), (float) -(player.getPosition().y - floor(player.getPosition().y)), (float) -(player.getPosition().z - floor(player.getPosition().z)));
//        pMat.setPerspective((float) toRadians(GameProperties.FOV_DEGREES), (float) width / height, useInverseDepth ? GameProperties.FAR : GameProperties.NEAR, useInverseDepth ? GameProperties.NEAR : GameProperties.FAR, useInverseDepth);
//        pMat.mulPerspectiveAffine(vMat, mvpMat);
//        mvpMat.invert(imvpMat);
//        player.getDirection().x = angx;
//        player.getDirection().y = angy;
//        player.getDirection().z = angz;
//        updateFrustumPlanes();
    }

    /**
     * Update the plane equation coefficients for the frustum planes from the { mvpMat}.
     */
//    private void updateFrustumPlanes() {
//        Matrix4f m = camera.getMvp();
//        FrustumPlanes frustumPlanes = gam.getFrustumPlanes();
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


//    /**
//     * Determine whether the player's eye is currently inside the given chunk.
//     */
//    private boolean playerInsideChunk(Chunk chunk) {
//        float margin = GameProperties.CHUNK_SIZE * 0.5f;
//        int minX = chunk.cx << GameProperties.CHUNK_SIZE_SHIFT, maxX = minX + GameProperties.CHUNK_SIZE;
//        int minZ = chunk.cz << GameProperties.CHUNK_SIZE_SHIFT, maxZ = minZ + GameProperties.CHUNK_SIZE;
//        return player.getPosition().x + margin >= minX && player.getPosition().x - margin <= maxX && player.getPosition().z + margin >= minZ && player.getPosition().z - margin <= maxZ;
//    }


    /**
     * Setup GL state prior to drawing the chunks with an MDI call where the MDI structs are generated
     * by the CPU.
     */
//    private void preDrawChunksIndirectCpuGeneratedState() {
//        renderedWorldArea.preDrawChunksState();
//        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, renderedWorldArea.indirectDrawBuffer);
//        int uboSize = roundUpToNextMultiple(chunksProgramUboSize, uniformBufferOffsetAlignment);
//        glBindBufferRange(GL_UNIFORM_BUFFER, chunksProgramUboBlockIndex, chunksProgramUbo, (long) currentDynamicBufferIndex * uboSize, uboSize);
//    }

    /**
     * Draw all chunks via a single MDI glMultiDrawElementsIndirect() call with MDI structs written by
     * the CPU.
     */
//    private void drawChunksWithMultiDrawElementsIndirectCpuGenerated(int numChunks) {
//        if (PerFaceBuffers.chunksVao == 0) {
//            return;
//        }
//        renderedWorldArea.updateChunksProgramUbo();
//        preDrawChunksIndirectCpuGeneratedState();
//        glMultiDrawElementsIndirect(drawPointsWithGS ? GL_POINTS : GL_TRIANGLE_STRIP, GL_UNSIGNED_SHORT, (long) GameProperties.MAX_ACTIVE_CHUNKS * currentDynamicBufferIndex * 5 * Integer.BYTES, numChunks, 0);
//    }


    /**
     * Update the uniform buffer object for the bounding boxes program.
     */
    private void updateBoundingBoxesProgramUbo() {
        int size = roundUpToNextMultiple(boundingBoxesProgramUboSize, uniformBufferOffsetAlignment);
        try (MemoryStack stack = stackPush()) {
            long ubo, uboPos;
            if (useBufferStorage) {
                ubo    = boundingBoxesProgramUboAddr;
                uboPos = currentDynamicBufferIndex * size;
            } else {
                ubo    = stack.nmalloc(size);
                uboPos = 0L;
            }
            camera.getMvp().getToAddress(ubo + uboPos);
            uboPos += 16 * Float.BYTES;
            memPutInt(ubo + uboPos, (int) floor(player.getPosition().x));
            memPutInt(ubo + uboPos + Integer.BYTES, (int) floor(player.getPosition().y));
            memPutInt(ubo + uboPos + Integer.BYTES * 2, (int) floor(player.getPosition().z));
            uboPos += 3 * Integer.BYTES;
            glBindBuffer(GL_UNIFORM_BUFFER, boundingBoxesProgramUbo);
            if (useBufferStorage) {
                glFlushMappedBufferRange(GL_UNIFORM_BUFFER, (long) currentDynamicBufferIndex * size, size);
            } else {
                nglBufferSubData(GL_UNIFORM_BUFFER, (long) currentDynamicBufferIndex * size, uboPos, ubo);
            }
        }
    }

    public static int rgb(int r, int g, int b) {
        return r | g << 8 | b << 16 | 0xFF << 24;
    }

    /**
     * Loop in the main thread to only process OS/window event messages.
     * <p>
     * See {link #registerWindowCallbacks()} for all callbacks that may fire due to events.
     */
    private void runWndProcLoop(Starter starter) {
        long window = starter.gameInstance.getWindowManager().getWindow();
        glfwShowWindow(window);
        while (!glfwWindowShouldClose(window)) {
            glfwWaitEvents();
            if (updateWindowTitle) {
                glfwSetWindowTitle(window, windowStatsString);
                updateWindowTitle = false;
            }
        }
    }

    /**
     * Compute last frame's dynamic buffer index.
     * <p>
     * This will be used for temporal coherence occlusion culling to draw last frame's visible chunks.
     */
    private int lastFrameDynamicBufferIndex() {
        return (currentDynamicBufferIndex + GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS - 1) % GameProperties.DYNAMIC_BUFFER_OBJECT_REGIONS;
    }

    /**
     * Run the "update and render" loop in a separate thread.
     * <p>
     * This is to decouple rendering from possibly long-blocking polling of OS/window messages (via
     * {@link GLFW#glfwPollEvents()}).
     */
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
////                updateStatsInWindowTitle(dt);
//            }
//            /*
//             * Execute any runnables that have accumulated in the render queue. These are GL calls for
//             * created/updated chunks.
//             */
//            drainRunnables();
//            /*
//             * Bind FBO to which we will render.
//             */
////            glBindFramebuffer(GL_FRAMEBUFFER, fbo);
//
//            /*
//             * If we don't want to do any sort of occlusion culling, we clear color and depth buffers and update
//             * the player's position and matrices.
//             */
////            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//            /*
//             * Update player's position and matrices.
//             */
//            updatePlayerPositionAndMatrices(dt);
// /*
////                     * Create new in-view chunks and destroy out-of-view chunks.
////                     */
////            renderedWorldArea.createInRenderDistanceAndDestroyOutOfRenderDistanceChunks();
////            renderedWorldArea.rebuildMeshForUpdatedChunks();
////            determineSelectedVoxel();
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
//
////            particlesRenderer.render(player.getPosition(), camera.getMvp());
////            virtualPlaneRender.draw(player.getPosition(), camera.getMvp());
//
////            if (1 == 1) {
////                profile("drawChunksWithMultiDrawElementsBaseVertex", renderedWorldArea::drawChunksWithMultiDrawElementsBaseVertex);
////            }
////            profile("drawSelection", this::drawSelection);
//
////            boxRenderer.render(new ArrayList<>(Arrays.asList(player.getHitbox())));
////            particlesRenderer.render(new Vector3f(), camera.getMvp(),camera);
//
//
//            if (showGui == 1) {
//                texturedQuadsRendererFun.render();
//            }
////            freePane.getPos().x = (float) -floor(player.getPosition().x);
////            freePane.getPos().y = -(float) floor(player.getPosition().y);
////            freePane.getPos().z = -(float) floor(player.getPosition().z);
//
////            freePane.render();
//            centerCross.render();
////            letterQuadsRenderer.render();
////                }
////            }
//            /*
//             * Draw highlighting of selected voxel face.
//             */
//
////            profile("drawGUI", this::drawGUI);
//
////            textAreaRenderer.render();
////            textAreaRenderer.addLine((fly ? "FLY! " : "walk ") + " X:" + round(player.getVelocity().x) + " Y:" + round(player.getPosition().y) + " Z:" + round(player.getPosition().z) + " MAT:" + selectedMaterial.getId());
////            profile("drawGUI", this::drawGUI);
////            System.out.println("GameTick:"+gameTickTst.getDeltaFromLastTick());
//
////            Vector3d dc = new Vector3d(player.getPosition());
////            voxelRender.draw(dc, mvpMat);
////            for (int di = 0; di < 64; di++) {
////                voxelRender.draw(dc.add(16, 0, 0), mvpMat);
////            }
//
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

    private          int     statsFrames;
    private          float   statsTotalFramesTime;
    private volatile boolean updateWindowTitle;
    private          String  windowStatsString;

    /**
     * When in windowed mode, this method will be called to update certain statistics that are shown in the window
     * title.
     */
//    private void updateStatsInWindowTitle(float dt) {
//        if (statsTotalFramesTime >= 0.5f) {
//            int px = (int) floor(player.getPosition().x);
//            int py = (int) floor(player.getPosition().y);
//            int pz = (int) floor(player.getPosition().z);
//            windowStatsString    = statsFrames * 2 + " FPS, "
//                    + GameProperties.INT_FORMATTER.format(worldMap.getChunkManager().count()) + " act. chunks, "
//                    + GameProperties.INT_FORMATTER.format(renderedWorldArea.getInFrustrum()) + " chunks in frustum, GPU mem. "
//                    + GameProperties.INT_FORMATTER.format(worldMap.getChunkManager().computePerFaceBufferObjectSize() / 1024 / 1024) + " MB @ "
//                    + px + " , " + py + " , " + pz;
//            statsFrames          = 0;
//            statsTotalFramesTime = 0f;
//            updateWindowTitle    = true;
//            glfwPostEmptyEvent();
//        }
//        statsFrames++;
//        statsTotalFramesTime += dt;
//    }

    /**
     * Process all update/render thread tasks in the {link #updateAndRenderRunnables} queue.
     */
    private void drainRunnables() {
        Iterator<DelayedRunnable> it = updateAndRenderRunnables.iterator();
        while (it.hasNext()) {
            DelayedRunnable dr = it.next();
            /* Check if we want to delay this runnable */
            if (dr.delay > 0) {
                if (GameProperties.DEBUG) {
                    System.out.println("Delaying runnable [" + dr.name + "] for " + dr.delay + " frames");
                }
                dr.delay--;
                continue;
            }
            try {
                /* Remove from queue and execute */
                it.remove();
                dr.runnable.call();
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }
    }

    /**
     * Configure OpenGL state for drawing the selected voxel face.
     */
    private void preDrawSelectionState() {
        glDisable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glEnable(GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(useInverseDepth ? 1 : -1, useInverseDepth ? 1 : -1);
        glBindVertexArray(nullVao);
        glUseProgram(selectionProgram);
    }

    /**
     * Update the current region of the UBO for the drawing the selection quad.
     */
    private void updateSelectionProgramUbo(Matrix4f mvp, Vector3f positionWithOffset, float r, float g, float b) {
        /* Round up to the next multiple of the UBO alignment */
        int size = roundUpToNextMultiple(selectionProgramUboSize, uniformBufferOffsetAlignment);
        try (MemoryStack stack = stackPush()) {
            long ubo, uboPos;
            if (useBufferStorage) {
                ubo    = selectionProgramUboAddr;
                uboPos = currentDynamicBufferIndex * size;
            } else {
                ubo    = stack.nmalloc(size);
                uboPos = 0L;
            }
            mvp.getToAddress(ubo + uboPos);
            uboPos += 16 * Float.BYTES;
            positionWithOffset.getToAddress(ubo + uboPos);
            uboPos += 1 * Float.BYTES;
            glBindBufferRange(GL_UNIFORM_BUFFER, selectionProgramUboBlockIndex, selectionProgramUbo, (long) currentDynamicBufferIndex * size, size);
            if (useBufferStorage) {
                glFlushMappedBufferRange(GL_UNIFORM_BUFFER, (long) currentDynamicBufferIndex * size, size);
            } else {
                nglBufferSubData(GL_UNIFORM_BUFFER, (long) currentDynamicBufferIndex * size, uboPos, ubo);
            }
        }
    }

    /**
     * Update the current region of the UBO for the drawing the selection quad.
     */
    private void updateGUIProgramUbo(Matrix4f mvp) {
        /* Round up to the next multiple of the UBO alignment */
        int size = roundUpToNextMultiple(4 * (16 + 4 + 4), uniformBufferOffsetAlignment);
        try (MemoryStack stack = stackPush()) {
            long ubo, uboPos;
//            if (useBufferStorage) {
//                ubo = selectionProgramUboAddr;
//                uboPos = currentDynamicBufferIndex * size;
//            } else {
            ubo    = stack.nmalloc(size);
            uboPos = 0L;
//            }
            mvp.getToAddress(ubo + uboPos);
            uboPos += 16 * Float.BYTES;
            //            System.out.println("color index"+selectedMaterial+" rgb:"+color);
            Vector4f grid = new Vector4f(GameDynamicProperties.guiGridHalfSizeGlView, 0, GameDynamicProperties.guiGridHalfSizeInScreenPixel);
            grid.getToAddress(ubo + uboPos);
            uboPos += 4 * Float.BYTES;

            new Vector4f(0, 0, 0,
                    selectedMaterial.getFaceMaterial()[0].getTextureInfo().getTextureArrayId())
                    .getToAddress(ubo + uboPos);
            uboPos += 4 * Float.BYTES;
            glBindBufferRange(GL_UNIFORM_BUFFER, guiProgramUboBlockIndex, guiProgramUbo, (long) currentDynamicBufferIndex * size, size);
            if (useBufferStorage) {
                glFlushMappedBufferRange(GL_UNIFORM_BUFFER, (long) currentDynamicBufferIndex * size, size);
            } else {
                nglBufferSubData(GL_UNIFORM_BUFFER, (long) currentDynamicBufferIndex * size, uboPos, ubo);
            }
        }
    }

    /**
     * Draw the highlighting of the selected voxel face.
     */
    private void drawSelection() {
        if (!hasSelection) {
            return;
        }
        preDrawSelectionState();
        /* compute a player-relative position. The MVP matrix is already player-centered */
        double dx = selectedVoxelPosition.getX() - (player.getPosition().x);
        double dy = selectedVoxelPosition.getY() - (player.getPosition().y);
        double dz = selectedVoxelPosition.getZ() - (player.getPosition().z);
        tmpMat.set(camera.getMvp()).translate((float) dx, (float) dy, (float) dz);
        /* translate and rotate based on face side */
        // TODO Магия матриц преобразования, может когда-нибудь разберусь ...
        if (sideOffset != null) {
            if (sideOffset.getDx() != 0) {
                tmpMat.translate(sideOffset.getDx() > 0 ? 1 : 0, 0, 1).mul3x3(0, 0, -1, 0, 1, 0, 1, 0, 0);
            } else if (sideOffset.getDy() != 0) {
                tmpMat.translate(0, sideOffset.getDy() > 0 ? 1 : 0, 1).mul3x3(1, 0, 0, 0, 0, -1, 0, 1, 0);
            } else if (sideOffset.getDz() != 0) {
                tmpMat.translate(0, 0, sideOffset.getDz() > 0 ? 1 : 0).mul3x3(1, 0, 0, 0, 1, 0, 0, 0, 1);
            }
        }
        /* animate it a bit */
        float s = (float) sin(System.currentTimeMillis() / 4E2);
        tmpMat
                .translate(0.5f, 0.5f, 0f)
                .scale(0.3f + 0.1f * s * s);
        updateSelectionProgramUbo(tmpMat, camera.getPosition(), 0.2f, 0.3f, 0.6f);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        postDrawSelectionState();

    }

    /**
     * Draw the highlighting of the selected voxel face.
     */
    private void drawGUI() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
//        glEnable(GL_POLYGON_OFFSET_FILL);
        glBindVertexArray(nullVao);
        glUseProgram(guiProgram);

//        GOOD
        Matrix4f scale = new Matrix4f(
                0.1f, 0f, 0f, 0f,
                0f, 0.18f, 0f, 0f,
                0f, 0f, -0f, 1f,
                -0.9f, -0.83f, -0f, 1f);
//      scale=  new Matrix4f()
//                .ortho(-1, 1, -1, 1f, 1f, -1f)
//              .scaleAround(0.5f, 0.5f, 2f, 0, 0f, 0);
//         Matrix for all screen
//        Matrix4f scale = new Matrix4f().ortho(-1f,1,-1,1f,-0f,-0.1f);


//        tmpMat.scale(1.2f, scale);
//        System.out.println(tmpMat);
        updateGUIProgramUbo(scale);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 8);

        glDisable(GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(0, 0);
    }

    /**
     * Reset of critical global GL state, that we cannot assume the next draw call to reset itself,
     * after {@link #drawSelection()}.
     */
    private void postDrawSelectionState() {
        glDisable(GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(0, 0);
    }

    /**
     * Create a dedicated thread to process updates and perform rendering.
     * <p>
     * This is <em>only</em> for decoupling the render thread from potentially long-blocking
     * {@link GLFW#glfwPollEvents()} calls (when e.g. many mouse move events occur).
     * <p>
     * Instead, whenever a OS/window event is received, it is enqueued into the
     * {link #updateAndRenderRunnables} queue.
     */
//    private Thread createAndStartUpdateAndRenderThread(Starter starter) {
//        Thread renderThread = new Thread(this::runUpdateAndRenderLoop);
//        renderThread.setName("Render Thread");
//        renderThread.setPriority(Thread.MAX_PRIORITY);
////        renderThread.start();
//        starter.getThreadManager().addAndRunThread(renderThread);
//        Profiler.start();
//        return renderThread;
//    }

//    private Thread createAndStartServerThread() {
//        Thread renderThread = new Thread(this::runUpdateAndRenderLoop);
//        renderThread.setName("Render Thread");
//        renderThread.setPriority(Thread.MAX_PRIORITY);
//        renderThread.start();
//        Profiler.start();
//        return renderThread;
//    }

    /**
     * Initialize and run the game/demo.
     */
    private void run() throws InterruptedException, IOException {
        Starter starter = new Starter();
        starter.createLocalGame();
//        if (!glfwInit()) {
//            throw new IllegalStateException("Unable to initialize GLFW");
//        }

//        createWindow();
//        registerWindowCallbacks();
//        setWindowPosition();
//        queryFramebufferSizeForHiDPI();
        GameTick.start();

//        player.setCollisionDetector(collisionDetector);

//        glfwMakeContextCurrent(window);

        /* Determine, which additional OpenGL capabilities we have. */
//        determineOpenGLCapabilities();
//        glfwSwapInterval(GameProperties.VSYNC ? 1 : 0);

        createNullVao();

//        Singletons.initSingletons();
//        initGLResources();

//        glfwSetCursorPosCallback(window, mouseInputHandler::onCursorPos);

        /* Временные затычки при рефакторинге**/
//        worldMap.player      = player;
        ChunkDistance.player = starter.getGameInstance().getPlayers().get(0);

        /* Временные затычки при рефакторингеEND**/
        // Создание чанков заранее
//        worldMap.getChunkHolder().getChunkByCoordinate(0, 0);
//        for (int x = 0; x < 5; x++) {
//            for (int y = 0; y < 5; y++) {
//                worldMap.getChunkHolder().ensureChunk(x, y);
//                worldMap.getChunkHolder().ensureChunk(-x, -y);
//                worldMap.getChunkHolder().ensureChunk(-x, y);
//                worldMap.getChunkHolder().ensureChunk(x, -y);
//            }
//        }

        //TODO temporary
        player = starter.getGameInstance().getPlayers().get(0);
        camera = starter.getGameInstance().getCameras().get(0);
        /* Run logic updates and rendering in a separate thread */
//        Thread updateAndRenderThread = createAndStartUpdateAndRenderThread(starter);
        /* Process OS/window event messages in this main thread */
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        /* Wait for the latch to signal that init render thread actions are done */
//        runWndProcLoop(starter);
        /*
         * After the wnd loop exited (because the window was closed), wait for render thread to complete
         * finalization.
         */
//        updateAndRenderThread.join();
        if (debugProc != null) {
            debugProc.free();
        }
//        glfwFreeCallbacks(window);
//        glfwDestroyWindow(window);
//        glfwTerminate();
        starter.start();

        starter.stop();
    }

    private void initGLResources() throws IOException {


        /*
         * Compute number of vertices per face and number of bytes per vertex. These depend on the features
         * we are going to use.
         */
//        PerFaceBuffers.verticesPerFace = drawPointsWithGS ? 1 : 4;
//        PerFaceBuffers.indicesPerFace  = drawPointsWithGS ? 1 : 5;
//        PerFaceBuffers.voxelVertexSize = drawPointsWithGS ? 2 * Integer.BYTES : Integer.BYTES + Short.BYTES + (!useMultiDrawIndirect ? Integer.BYTES : 0);

        if (GameProperties.DEBUG || GameProperties.GLDEBUG) {
            installDebugCallback();
        }

//        TextureDictionary.getInstance().init();
//        BlockTypeDictionary.getInstance().init();
        selectedMaterial = BlockTypeDictionary.DEFAULT_BLOCK;
        //Загрузка шрифтов
        List<String> letterSet = new ArrayList<>();
        letterSet.add("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        letterSet.add("abcdefghijklmnopqrstuvwxyz");
        letterSet.add("0123456789&'()*+-=.!\"#$%^§");
        letterSet.add(",:;?@/<>|\\~~[]{}_~ ");
        Font localFont = new Font("wCvnX.png", letterSet);
//        font = new FontShaderProgram(localFont);

//        grassTextureId = TextureLoader.loadTexture("grass.png").getTextureId();
        grassTextureId = TextureLoader.loadTexture("grass.png");


        particlesRenderer        = new ParticlesRenderer()
                .withMvp(camera.getMvp())
                .initialize();
        texturedQuadsRendererFun = new TexturedQuadsRendererFun()
                .withMvp(new Matrix4f()
                                .ortho(-1, 1, -1, 1f, 1f, -1f)
                        //.scaleAround(1.0f, 1.0f, 1f, 0, 1f, 0)
                )
                .initialize();
        freePane                 = new Free3DObjectRenderer()
                .withMvp(camera.getMvp())
                .initialize();
        centerCross              = new TexturedQuadsRenderer().initialize();
        centerCross.getItems().clear();
        centerCross.getItems().add(new TexturedQuadsItem(new Vector4f(-0.002f, 0.003f, 0.002f, -0.003f),
                new Vector4f(0, 0, 1, 1),
                new Vector4f(0, 0, 1, 0.5f)));
//        letterQuadsRenderer = new LetterQuadsRenderer(localFont, 16).withMvp(new Matrix4f()).initialize();
        textAreaRenderer = new TextAreaRenderer(localFont, 40, 15);
        textAreaRenderer.withMvp(new Matrix4f()/*.ortho(0,1,-1,0.5f,-1,1)*/.scaleAround(0.3f, -1, 1, 0)).initialize();
        textAreaRenderer.setLines(new ArrayList<>(Arrays.asList("qwtqoqeofqtiqueeiuteqriuheufietqiutyqituyertiueyrt", "123123QWEQWE!@#wedw.,.,.", "--------------------------")));

        voxelRender = new VoxelRender();
        /* Configure OpenGL state and create all necessary resources */
        configureGlobalGlState();
        createSelectionProgram();
        createSelectionProgramUbo();
//        createMaterials();
//        renderedWorldArea.createMultiDrawIndirectBuffer();

        ChunkShaderProgram csp = new ChunkShaderProgram();
        csp.init();
        chunksProgram              = csp.getId();
        chunksProgramUboBlockIndex = csp.getUniformStd140List().get(0).getId();
        chunksProgramUbo           = csp.getUniformStd140List().get(0).getBo();
//        createChunksProgram();
//        createChunksProgramUbo();
//        if (canGenerateDrawCallsViaShader || true) {
//        createOcclusionCullingBufferObjects();
//        createBoundingBoxesProgram();
//        createCollectDrawCallsProgram();
//        createBoundingBoxesProgramUbo();
//        createBoundingBoxesVao();
//        }


//        createGUIVao();
        createGUIProgram();
        createGUIProgramUbo();


//        createFramebufferObject();
//        textAreaRenderer = new TextAreaRenderer(font)
//                .withMvp(new Matrix4f().ortho(-1, 1, -1, 1, -1, 1)
////                        .scaleLocal(0.5f)
//                )
//                .initialize();


        /* Make sure everything is ready before we show the window */
        glFlush();
        glfwMakeContextCurrent(NULL);
        GL.setCapabilities(null);
    }

    public static void main(String[] args) throws Exception {
        new VoxelGame2GL().run();
        /**       VoxelField vf = new VoxelField();
         int sliceU = 1;
         int sliceV = 1;
         vf.updateBitMaskInRow2(sliceU, sliceV, 2, 0x3F, XP, 0x00);
         vf.updateBitMaskInRow2(sliceU, sliceV, 2, 0x3F, XM, 0x00);
         logSlice(vf, sliceU, sliceV);
         vf.updateBitMaskInRow2(sliceU, sliceV, 3, 0x3F, XP, 0x02);
         vf.updateBitMaskInRow2(sliceU, sliceV, 3, 0x3F, XM, 0x02);
         logSlice(vf, sliceU, sliceV);
         vf.updateBitMaskInRow2(sliceU, sliceV, 6, 0x3F, XP, 0x00);
         vf.updateBitMaskInRow2(sliceU, sliceV, 6, 0x3F, XM, 0x00);
         logSlice(vf, sliceU, sliceV);
         vf.updateBitMaskInRow2(sliceU, sliceV, 5, 0x3F, XP, 0x08);
         vf.updateBitMaskInRow2(sliceU, sliceV, 5, 0x3F, XM, 0x08);
         logSlice(vf, sliceU, sliceV);
         vf.updateBitMaskInRow2(sliceU, sliceV, 4, 0x3F, XP, 0x0A);
         vf.updateBitMaskInRow2(sliceU, sliceV, 4, 0x3F, XM, 0x0A);
         logSlice(vf, sliceU, sliceV);
         //        vf.updateBitMaskInRow2(sliceU, sliceV, 3, 0x0, XP, 0x00);
         //        vf.updateBitMaskInRow2(sliceU, sliceV, 3, 0x0, XM, 0x00);
         //        logSlice(vf, sliceU, sliceV);

         // new row
         sliceU=0;
         vf.updateBitMaskInRow2(sliceU, sliceV, 4, 0x3F, XP, 0x00);
         logSlice(vf, sliceU, sliceV);

         //
         //        for (int i = Integer.MAX_VALUE-10; i < Integer.MAX_VALUE; i++) {
         //
         //            long z = ExtMath.doubleBits(i);
         //            System.out.println(String.format("%64s", Long.toBinaryString(i)).replace(' ', '0') + " <-" + i);
         //            System.out.println(String.format("%64s", Long.toBinaryString(z)).replace(' ', '0') + " <-" + z);
         //            System.out.println();
         //        }


         //        for(int i=0;i<=16;i++) {
         //            Vector3f v = new Vector3f((i&1) *2,(i>>1 & 1)*2 , 1);
         //            Vector3f v1 = new Vector3f(1);
         //            System.out.println("i:"+i+": "+ v.sub(v1));
         //        }
         */
    }

    private static void logSlice(VoxelField vf, int sliceU, int sliceV) {
        System.out.println(getSolidString32BitMask(vf, sliceU, sliceV) + " <- solid current");
        System.out.println(getGlueString64BitMask(vf, sliceU, sliceV) + " <- glue current");
        System.out.println(getSolidString32BitMask(vf, (sliceU + XM.getD()), sliceV) + " <- solid opposite XM");
        System.out.println(getGlueString64BitMask(vf, (sliceU + XM.getD()), sliceV) + " <- glue opposite XM");
        System.out.println(getSolidString32BitMask(vf, (sliceU + XP.getD()), sliceV) + " <- solid opposite XP");
        System.out.println(getGlueString64BitMask(vf, (sliceU + XP.getD()), sliceV) + " <- glue opposite XP");
        System.out.println("--");
    }

    private static String getGlueString64BitMask(VoxelField vf, int sliceU, int sliceV) {
        return String.format("%64s", Long.toBinaryString(vf.getMeshingDataField()[(XP.getI() << 10) + (sliceU << 5) + sliceV])).replace(' ', '0').replaceAll("..", "$0 ");
    }

    private static String getSolidString32BitMask(VoxelField vf, int sliceU, int sliceV) {
        return String.format("%32s", Long.toBinaryString(vf.getSolidFaceField()[(XP.getI() << 10) + (sliceU << 5) + sliceV])).replace(' ', '0').replaceAll(".", ".$0 ");
    }


}
