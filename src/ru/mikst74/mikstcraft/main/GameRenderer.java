package ru.mikst74.mikstcraft.main;

import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import ru.mikst74.mikstcraft.model.font.Font;
import ru.mikst74.mikstcraft.render.RenderedWorldArea;
import ru.mikst74.mikstcraft.render.selectedvoxel.SelectedVoxelRenderer;
import ru.mikst74.mikstcraft.render.shader.texturedquads.TexturedQuadsItem;
import ru.mikst74.mikstcraft.render.text.TextAreaRenderer;
import ru.mikst74.mikstcraft.render.texturedquads.TexturedQuadsRenderer;
import ru.mikst74.mikstcraft.settings.GameProperties;
import ru.mikst74.mikstcraft.texture.TextureInfo;
import ru.mikst74.mikstcraft.util.DelayedRunnable;
import org.lwjgl.opengl.GL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static ru.mikst74.mikstcraft.util.BackgroundExecutor.updateAndRenderRunnables;
import static ru.mikst74.mikstcraft.util.OpenGLErrorChecker.check;
import static ru.mikst74.mikstcraft.util.time.Profiler.profile;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.*;

public class GameRenderer {
    private       long              lastTime;
    private final GameInstance      gameInstance;
    @Setter
    private       RenderedWorldArea renderedWorldArea;

    @Setter
    private WindowManager    windowManager;
    private TextAreaRenderer textAreaRenderer;

    private TexturedQuadsRenderer texturedQuadsRenderer;

    @Setter
    private SelectedVoxelRenderer selectedVoxelRenderer;

    public GameRenderer(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public Thread createThread() {

        List<String> letterSet = new ArrayList<>();
        letterSet.add("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        letterSet.add("abcdefghijklmnopqrstuvwxyz");
        letterSet.add("0123456789&'()*+-=.!\"#$%^§");
        letterSet.add(",:;?@/<>|\\~~[]{}_~ ");
        Font localFont = new Font("wCvnX.png", letterSet);

        textAreaRenderer = new TextAreaRenderer(localFont, 40, 15);
        textAreaRenderer.withMvp(new Matrix4f()/*.ortho(0,1,-1,0.5f,-1,1)*/.scaleAround(0.3f, -1, 1, 0)).initialize();
        textAreaRenderer.setLines(new ArrayList<>(Arrays.asList("qwtqoqeofqtiqueeiuteqriuheufietqiutyqituyertiueyrt", "123123QWEQWE!@#wedw.,.,.", "--------------------------")));

        texturedQuadsRenderer = new TexturedQuadsRenderer();
        TextureInfo renderTexture = TextureInfo.builder()
                .textureId(windowManager.getRenderTextureId())
                .build();

        texturedQuadsRenderer.getShaderProgram().linkTexture(0, renderTexture);
        TexturedQuadsItem tqi = new TexturedQuadsItem(new Vector4f(-1, -1, 1, 1), new Vector4f(0, 0, 1, 1), new Vector4f(0));
        texturedQuadsRenderer.getItems().add(tqi);

        selectedVoxelRenderer = new SelectedVoxelRenderer();
        selectedVoxelRenderer.linkToCamera(gameInstance.getCurrentCamera());
        selectedVoxelRenderer.linkToSelectedVoxel(gameInstance.getCurrentPlayer().getSelectionDetector().getSelectedVoxel());


        /// - **
        Thread renderThread = new Thread(this::renderLoop);
        renderThread.setName("Render Thread");
        renderThread.setPriority(Thread.MAX_PRIORITY);
        return renderThread;
    }


    public void renderLoop() {
        glfwMakeContextCurrent(windowManager.getWindow());
        GL.setCapabilities(windowManager.getCaps());

        while (!Thread.currentThread().isInterrupted()) {
//            renderedWorldArea.getPosition().x+=0.4f;
            drainRunnables();

            int l = 0;
//            System.out.println("********************************");
            glBindFramebuffer(GL_FRAMEBUFFER, windowManager.getFbo());
            glViewport(0, 0, windowManager.getRenderWidth(), windowManager.getRenderHeight());
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            renderedWorldArea.createInRenderDistanceAndDestroyOutOfRenderDistanceChunks();
            renderedWorldArea.rebuildMeshForUpdatedChunks();
            if (1 == 1) {
                profile("drawChunksWithMultiDrawElementsBaseVertex", renderedWorldArea::drawChunksWithMultiDrawElementsBaseVertex);
            }
            check(l++);
            selectedVoxelRenderer.render();
            check(l++);
            textAreaRenderer.addLine("pos:" + renderedWorldArea.getPosition());
            textAreaRenderer.render();
            check(l++);
//            profile("drawSelection", this::drawSelection);

// Bind default framebuffer (window) and set window's viewport
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glViewport(0, 0, windowManager.getWidth() * 2, windowManager.getHeight() * 2);

// Clear the window's buffers
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

// Bind the FBO's texture
            glBindTexture(GL_TEXTURE_2D, windowManager.getRenderTextureId());
            glEnable(GL_CULL_FACE);
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
// ******* DRAW A FULL-SCREEN QUAD HERE *******
// Use a simple shader program that samples the bound texture and draws it
            texturedQuadsRenderer.render();
//            drawFullScreenQuadWithTexture();


//            glBindFramebuffer(GL_READ_FRAMEBUFFER, windowManager.getFbo());
//            check(l++);
//            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
//            check(l++);
//            glViewport(0, 0, windowManager.getWidth(), windowManager.getHeight());
//            check(l++);
//
            glBlitFramebuffer(0, 0, windowManager.getWidth(), windowManager.getHeight(),
                    0, 0, windowManager.getWidth(), windowManager.getHeight(),
                    GL_COLOR_BUFFER_BIT, GL_LINEAR);
//            check(l++);
            glfwSwapBuffers(windowManager.getWindow());
//            check(l++);
        }
    }

    private void updateStatsInWindowTitle() {
        long thisTime = System.nanoTime();
        float dt = (thisTime - lastTime) * 1E-9f;
        lastTime = thisTime;
//        if (!GameProperties.FULLSCREEN) {
//            /*
//             * Update stats in window title if we run in windowed mode.
//             */
//            if (statsTotalFramesTime >= 0.5f) {
//                int px = (int) floor(player.getPosition().x);
//                int py = (int) floor(player.getPosition().y);
//                int pz = (int) floor(player.getPosition().z);
//                windowStatsString    = statsFrames * 2 + " FPS, "
//                        + GameProperties.INT_FORMATTER.format(worldMap.getChunkManager().count()) + " act. chunks, "
//                        + GameProperties.INT_FORMATTER.format(renderedWorldArea.getInFrustrum()) + " chunks in frustum, GPU mem. "
//                        + GameProperties.INT_FORMATTER.format(worldMap.getChunkManager().computePerFaceBufferObjectSize() / 1024 / 1024) + " MB @ "
//                        + px + " , " + py + " , " + pz;
//                statsFrames          = 0;
//                statsTotalFramesTime = 0f;
//                updateWindowTitle    = true;
//                glfwPostEmptyEvent();
//            }
//            statsFrames++;
//            statsTotalFramesTime += dt;
//        }
    }

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
}
