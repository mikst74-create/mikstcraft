package ru.mikst74.mikstcraft.main;

import lombok.Getter;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryStack;
import ru.mikst74.mikstcraft.settings.GameDynamicProperties;
import ru.mikst74.mikstcraft.settings.GameProperties;
import ru.mikst74.mikstcraft.util.DelayedRunnable;

import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.ARBClipControl.GL_ZERO_TO_ONE;
import static org.lwjgl.opengl.ARBClipControl.glClipControl;
import static org.lwjgl.opengl.GL11C.glViewport;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL31C.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static ru.mikst74.mikstcraft.main.GameRenderer.RENDER_OVER_TEXTURE;
import static ru.mikst74.mikstcraft.settings.OpenGLProperties.*;
import static ru.mikst74.mikstcraft.util.BackgroundExecutor.updateAndRenderRunnables;

public class WindowManager {
    public static final float WINDOW_SCALE = 0.4f;
    @Getter
    private             long  window;
    @Getter
    private             int   width;
    @Getter
    private             int   height;
    @Getter
    private             int   renderWidth;
    @Getter
    private             int   renderHeight;

    @Getter
    private int renderTextureId;

    // frameBufferObject variables
    @Getter
    private int            fbo;
    private int            colorRbo;
    private int            depthRbo;
    @Getter
    private GLCapabilities caps;

    public void createWindow() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }


        setWindowHints();

        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidmode = glfwGetVideoMode(monitor);
        width  = (int) (Objects.requireNonNull(vidmode).width() * (GameProperties.FULLSCREEN ? 1 : WINDOW_SCALE));
        height = (int) (vidmode.height() * (GameProperties.FULLSCREEN ? 1 : WINDOW_SCALE));
        window = glfwCreateWindow(width, height, "MikstCraft!", GameProperties.FULLSCREEN ? monitor : NULL, NULL);
        GameDynamicProperties.useScreenSize(width, height);

        if (GameProperties.GRAB_CURSOR) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        }

        if (window == NULL) {
            throw new AssertionError("Failed to create the GLFW window");
        }

        setWindowPosition();
        queryFramebufferSizeForHiDPI();
        glfwSetFramebufferSizeCallback(window, this::onFramebufferSize);
        glfwMakeContextCurrent(window);
        /* Determine, which additional OpenGL capabilities we have. */
        determineOpenGLCapabilities();
        GL.setCapabilities(caps);

        glfwSwapInterval(GameProperties.VSYNC ? 1 : 0);

        createFramebufferObject();
        configureGlobalGlState();
//        glViewport(0, 0, renderWidth, renderHeight);

    }

    private static void setWindowHints() {
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        /*
         * Disable window framebuffer bits we don't need, because we render into offscreen FBO and blit to
         * window.
         */
        glfwWindowHint(GLFW_DEPTH_BITS, 0);
        glfwWindowHint(GLFW_STENCIL_BITS, 0);
//        glfwWindowHint(GLFW_ALPHA_BITS, 0);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
//        glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_TRUE);
        if (GameProperties.FULLSCREEN) {
            glfwWindowHint(GLFW_FLOATING, GLFW_TRUE);
            glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
        }
        if (GameProperties.DEBUG || GameProperties.GLDEBUG) {
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        }
    }

    private void setWindowPosition() {
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (Objects.requireNonNull(vidmode).width() - width) / 2, (vidmode.height() - height) / 2);
    }

    private void queryFramebufferSizeForHiDPI() {
        try (MemoryStack frame = stackPush()) {
            IntBuffer framebufferSize = frame.mallocInt(2);
            nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
            if (RENDER_OVER_TEXTURE) {
                renderWidth = framebufferSize.get(0) * 2;///2;// / 4;
                renderHeight = framebufferSize.get(1) * 2;///2 ;/// 4;
            } else {
                renderWidth  = framebufferSize.get(0);
                renderHeight = framebufferSize.get(1);
                width        = framebufferSize.get(0);
                height       = framebufferSize.get(1);

            }
        }
    }

    /**
     * GLFW framebuffer size callback.
     */
    private void onFramebufferSize(long window, int w, int h) {
        if (w <= 0 && h <= 0) {
            return;
        }
        updateAndRenderRunnables.add(new DelayedRunnable(() -> {
            width  = w;
            height = h;
            createFramebufferObject();
            glViewport(0, 0, width, height);
//            glViewport(0, 0, width, height);
            GameDynamicProperties.useScreenSize(width, height);
            return null;
        }, "Framebuffer size change", 0));
    }

    /**
     * We will render to an FBO.
     */
    private void createFramebufferObject() {
        /*
         * Delete any existing FBO (happens when we resize the window).
         */
        if (fbo != 0) {
            glDeleteFramebuffers(fbo);
            glDeleteRenderbuffers(new int[]{colorRbo, depthRbo});
        }
        fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);


        // Create the texture to render to
        renderTextureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, renderTextureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, renderWidth, renderHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR); // Nearest for pixelated upscale
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

// Attach the texture to the FBO
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, renderTextureId, 0);


        colorRbo = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, colorRbo);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, renderWidth, renderHeight);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, colorRbo);


//        if (useNvMultisampleCoverage) {
//            glRenderbufferStorageMultisampleCoverageNV(GL_RENDERBUFFER, GameProperties.COVERAGE_SAMPLES, GameProperties.COLOR_SAMPLES, GL_RGBA8, width, height);
//        } else {
//            glRenderbufferStorageMultisample(GL_RENDERBUFFER, GameProperties.COVERAGE_SAMPLES, GL_RGBA8, renderWidth, renderHeight);
//        }
//        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, colorRbo);
//        depthRbo = glGenRenderbuffers();
//        glBindRenderbuffer(GL_RENDERBUFFER, depthRbo);
//        if (useNvMultisampleCoverage) {
//            glRenderbufferStorageMultisampleCoverageNV(GL_RENDERBUFFER, GameProperties.COVERAGE_SAMPLES, GameProperties.COLOR_SAMPLES, GL_DEPTH_COMPONENT32F, width, height);
//        } else {
//            glRenderbufferStorageMultisample(GL_RENDERBUFFER, GameProperties.COVERAGE_SAMPLES, GL_DEPTH_COMPONENT32F, renderWidth, renderHeight);
//        }

        // Check if FBO is complete
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer not complete!");
            throw new RuntimeException("Framebuffer not complete!");
        }

//        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthRbo);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    /**
     * Query all (optional) capabilites/extensions that we want to use from the OpenGL context via
     * LWJGL's {@link GLCapabilities}.
     */
    private void determineOpenGLCapabilities() {
        caps                                     = GL.createCapabilities();
        useMultiDrawIndirect                     = caps.GL_ARB_multi_draw_indirect || caps.OpenGL43;
        useBufferStorage                         = caps.GL_ARB_buffer_storage || caps.OpenGL44;
        useClearBuffer                           = caps.GL_ARB_clear_buffer_object || caps.OpenGL43;
        drawPointsWithGS                         = useMultiDrawIndirect; // <- we just haven't implemented point/GS rendering without MDI yet
        useInverseDepth                          = caps.GL_ARB_clip_control || caps.OpenGL45;
        useNvMultisampleCoverage                 = caps.GL_NV_framebuffer_multisample_coverage;
        canUseSynchronousDebugCallback           = caps.GL_ARB_debug_output || caps.OpenGL43;
        canGenerateDrawCallsViaShader            = caps.GL_ARB_shader_image_load_store/* 4.2 */ && caps.GL_ARB_shader_storage_buffer_object/* 4.3 */ && caps.GL_ARB_shader_atomic_counters/* 4.2 */ || caps.OpenGL43;
        useOcclusionCulling                      = canGenerateDrawCallsViaShader && useMultiDrawIndirect;
        useTemporalCoherenceOcclusionCulling     = useOcclusionCulling && true;
        canSourceIndirectDrawCallCountFromBuffer = canGenerateDrawCallsViaShader && (caps.GL_ARB_indirect_parameters || caps.OpenGL46);
        useRepresentativeFragmentTest            = caps.GL_NV_representative_fragment_test;
        /* Query the necessary UBO alignment which we need for multi-buffering */
        uniformBufferOffsetAlignment = glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);

        System.out.println("useMultiDrawIndirect: " + useMultiDrawIndirect);
        System.out.println("useBufferStorage: " + useBufferStorage);
        System.out.println("drawPointsWithGS: " + drawPointsWithGS);
        System.out.println("useInverseDepth: " + useInverseDepth);
        System.out.println("useNvMultisampleCoverage: " + useNvMultisampleCoverage);
        System.out.println("canUseSynchronousDebugCallback: " + canUseSynchronousDebugCallback);
        System.out.println("canGenerateDrawCallsViaShader: " + canGenerateDrawCallsViaShader);
        System.out.println("useOcclusionCulling: " + useOcclusionCulling);
        System.out.println("useTemporalCoherenceOcclusionCulling: " + useTemporalCoherenceOcclusionCulling);
        System.out.println("canSourceIndirectDrawCallCountFromBuffer: " + canSourceIndirectDrawCallCountFromBuffer);
        System.out.println("useRepresentativeFragmentTest: " + useRepresentativeFragmentTest);
        System.out.println("uniformBufferOffsetAlignment: " + uniformBufferOffsetAlignment);
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
     * Loop in the main thread to only process OS/window event messages.
     * <p>
     * See {link #registerWindowCallbacks()} for all callbacks that may fire due to events.
     */
    public void runWndProcLoop() {
        glfwShowWindow(window);
        while (!glfwWindowShouldClose(window)) {
            glfwWaitEvents();
//            if (updateWindowTitle) {
//                glfwSetWindowTitle(window, windowStatsString);
//                updateWindowTitle = false;
//            }
        }
        System.out.println("exit");

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        GL.setCapabilities(null);
    }

    public void close() {
        glfwSetWindowShouldClose(window, true);
    }


}
