package ru.mikst74.mikstcraft.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.mikst74.mikstcraft.model.camera.Camera;
import ru.mikst74.mikstcraft.render.shader.particles.ParticlesShaderProgram;

import static java.lang.Math.floor;
import static java.lang.Math.sin;
import static ru.mikst74.mikstcraft.VoxelGame2GL.nullVao;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL32C.GL_PROGRAM_POINT_SIZE;

public class ParticlesRenderer {
    private final ParticlesShaderProgram shaderProgram;
    private       Matrix4f               mvp;
    private final Matrix4f               tmpMat     = new Matrix4f();
    private final Vector4f               timeVector = new Vector4f();

    public ParticlesRenderer() {
        shaderProgram = new ParticlesShaderProgram();
        shaderProgram.init();
    }

    public ParticlesRenderer withMvp(Matrix4f mvp) {
        this.mvp = mvp;
        return this;
    }

    public ParticlesRenderer initialize() {
        shaderProgram.activateShader();
        shaderProgram.updateProgramUbo(mvp, timeVector);
        shaderProgram.deactivateShader();

        return this;
    }

    public void render(Vector3f center, Matrix4f mvpMat, Camera camera) {


//        shaderProgram.activateShader();
//        shaderProgram.updateProgramUbo(mvp,timeVector);

        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glEnable(GL_POLYGON_OFFSET_FILL);
        glEnable(GL_PROGRAM_POINT_SIZE);
        glEnable(GL_DEPTH_TEST);
//        glEnable(GL_POLYGON_OFFSET_FILL);
//        glPolygonOffset(useInverseDepth ? 1 : -1, useInverseDepth ? 1 : -1);

        ////glUseProgram(programId);
        glBindVertexArray(nullVao);
        shaderProgram.activateShader();

        /* compute a player-relative position. The MVP matrix is already player-centered */
        double dx = /*selectedVoxelPosition.x */ 0 - floor(center.x);
        double dy = /*selectedVoxelPosition.y */ 3 - floor(center.y);
        double dz = /*selectedVoxelPosition.z */ 0 - floor(center.z);
        tmpMat.set(mvpMat).translate((float) dx, (float) dy, (float) dz);
        /* translate and rotate based on face side */
//        if (sideOffset.x != 0) {
//            tmpMat.translate(sideOffset.x > 0 ? 1 : 0, 0, 1).mul3x3(0, 0, -1, 0, 1, 0, 1, 0, 0);
//        } else if (sideOffset.y != 0) {
//            tmpMat.translate(0, sideOffset.y > 0 ? 1 : 0, 1).mul3x3(1, 0, 0, 0, 0, -1, 0, 1, 0);
//        } else if (sideOffset.z != 0) {
//            tmpMat.translate(0, 0, sideOffset.z > 0 ? 1 : 0).mul3x3(1, 0, 0, 0, 1, 0, 0, 0, 1);
//        }
        /* animate it a bit */
        float s = (float) sin((double) System.currentTimeMillis() / 567);
        // Move matrix to center voxel and zoom for animate
        tmpMat.translate(0.5f, 0.5f, 0.5f);//.scale(0.3f + 0.1f * s * s);
        camera.getMvp().translate(0.5f, 0.5f, 0.5f,tmpMat);//.scale(0.3f + 0.1f * s * s);
//        tmpMat.set(camera.getMvp());
        shaderProgram.updateProgramUbo(camera.getMvp(), timeVector);
//        shaderProgram.updateProgramUbo(tmpMat, timeVector);
//        glDrawArrays(GL_TRIANGLE_STRIP, 0, 1);
        glDrawArrays(GL_POINTS, 0, 1);
//        glDisable(GL_POLYGON_OFFSET_FILL);
//        glPolygonOffset(0, 0);


//        shaderProgram.deactivateShader();

    }


}
