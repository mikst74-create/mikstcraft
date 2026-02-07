package ru.mikst74.mikstcraft.render.box;

import lombok.Getter;
import lombok.Setter;
import ru.mikst74.mikstcraft.model.camera.Camera;
import ru.mikst74.mikstcraft.render.shader.box.BoxShaderProgram;
import ru.mikst74.mikstcraft.util.math.Hitbox;

import java.util.List;

@Getter
public class BoxRenderer {
    private final BoxShaderProgram shaderProgram;
    @Setter
    private  Camera camera;

    public BoxRenderer() {
        this.shaderProgram = new BoxShaderProgram(200);
        shaderProgram.init();
    }


    public BoxRenderer initialize() {
        shaderProgram.activateShader();
        shaderProgram.updateUbo(0,camera.getMvp());
        shaderProgram.deactivateShader();

        return this;
    }

    public void render(List<Hitbox> hitboxList) {

        shaderProgram.activateShader();

        shaderProgram.updateUbo(0, camera.getMvp(), camera.getPosition());
        shaderProgram.loadVbo(hitboxList);
        shaderProgram.render();

        shaderProgram.deactivateShader();

    }


}
