package ru.mikst74.mikstcraft.render.selectedvoxel;

import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import ru.mikst74.mikstcraft.model.NeighborCode;
import ru.mikst74.mikstcraft.model.SelectedVoxel;
import ru.mikst74.mikstcraft.model.camera.Camera;
import ru.mikst74.mikstcraft.render.shader.selectedvoxel.SelectedVoxelShaderProgram;

import static java.lang.Math.sin;

@Getter
public class SelectedVoxelRenderer {
    private final SelectedVoxelShaderProgram shaderProgram;
    private final Matrix4f                   mvp;
    @Setter
    private       SelectedVoxel              selectedVoxel;
    private       Camera                     camera;

    public SelectedVoxelRenderer() {
        this.shaderProgram = new SelectedVoxelShaderProgram();
        shaderProgram.init();
        this.mvp = new Matrix4f();
    }


    public SelectedVoxelRenderer linkToCamera(Camera camera) {
        this.camera = camera;
        return this;
    }

    public void linkToSelectedVoxel(SelectedVoxel selectedVoxel) {
        this.selectedVoxel = selectedVoxel;
    }


    public void render() {
        if (selectedVoxel != null && selectedVoxel.isHasSelection()) {
            recalcMatrices();

            shaderProgram.activateShader();

            shaderProgram.updateUbo(0, mvp, camera.getPosition());

            shaderProgram.render();

            shaderProgram.deactivateShader();
        }
    }

    private void recalcMatrices() {
        double dx = selectedVoxel.getCoo().getX() - (camera.getPosition().x);
        double dy = selectedVoxel.getCoo().getY() - (camera.getPosition().y);
        double dz = selectedVoxel.getCoo().getZ() - (camera.getPosition().z);
        mvp.set(camera.getMvp()).translate((float) dx, (float) dy, (float) dz);
        /* translate and rotate based on face side */
        // TODO Магия матриц преобразования, может когда-нибудь разберусь ...
        NeighborCode sideOffset = selectedVoxel.getSelectedFace();
        if (sideOffset != null) {
            if (sideOffset.getDx() != 0) {
                mvp.translate(sideOffset.getDx() > 0 ? 1 : 0, 0, 1).mul3x3(0, 0, -1, 0, 1, 0, 1, 0, 0);
            } else if (sideOffset.getDy() != 0) {
                mvp.translate(0, sideOffset.getDy() > 0 ? 1 : 0, 1).mul3x3(1, 0, 0, 0, 0, -1, 0, 1, 0);
            } else if (sideOffset.getDz() != 0) {
                mvp.translate(0, 0, sideOffset.getDz() > 0 ? 1 : 0).mul3x3(1, 0, 0, 0, 1, 0, 0, 0, 1);
            }
        }
        /* animate it a bit */
        float s = (float) sin(System.currentTimeMillis() / 4E2);
        mvp
                .translate(0.5f, 0.5f, 0f)
                .scale(0.3f + 0.1f * s * s);
    }


}
