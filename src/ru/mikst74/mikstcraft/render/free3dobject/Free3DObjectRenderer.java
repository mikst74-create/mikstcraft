package ru.mikst74.mikstcraft.render.free3dobject;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import ru.mikst74.mikstcraft.render.shader.free3dobject.Free3DObjectShaderProgram;
import ru.mikst74.mikstcraft.render.shader.free3dobject.Free3DObjectVertex;
import ru.mikst74.mikstcraft.texture.TextureLoader;

import java.util.ArrayList;
import java.util.List;

import static ru.mikst74.mikstcraft.render.shader.texturedquads.TexturedQuadsItem.color;
import static org.lwjgl.opengl.GL11C.*;

@Getter
public class Free3DObjectRenderer {
    private final Free3DObjectShaderProgram shaderProgram;
    private       Matrix4f                  mvp;
    private       Vector4f                 pos;
    private       List<Free3DObjectVertex> items;

    public Free3DObjectRenderer() {
        this.shaderProgram = new Free3DObjectShaderProgram(2000);
        shaderProgram.init();
        shaderProgram.linkTexture(0,TextureLoader.loadTexture("lightning-bolt.jpg"));
        this.items = new ArrayList<>();
        this.mvp   = new Matrix4f();
        this.pos   = new Vector4f();
    }

    public Free3DObjectRenderer withMvp(Matrix4f mvp) {
        this.mvp = mvp;
        return this;
    }

    public Free3DObjectRenderer withPos(Vector4f pos) {
        this.pos = pos;
        return this;
    }

    public Free3DObjectRenderer initialize() {
        shaderProgram.activateShader();
        shaderProgram.updateUbo(0, mvp, pos);
        shaderProgram.deactivateShader();

        items.add(new Free3DObjectVertex(new Vector4f(-10,23,44,0),
                new Vector4f(0,0,0,0),
                color(1, 0, 0, 0)));
        items.add(new Free3DObjectVertex(new Vector4f(0,0,20,0),
                new Vector4f(1,0,0,0),
                color(1, 0, 0, 0)));
        items.add(new Free3DObjectVertex(new Vector4f(0,00,0,0),
                new Vector4f(0.5f,1f,0,0),
                color(1, 0, 0, 0)));
//        items.add(new TexturedQuadsItem(pos(-0.8f, 0.8f, 0.8f, -0.8f), pos(0, 0, 1, 1)));
//        items.add(new TexturedQuadsItem(pos(0.9f, 1f, 1.0f, 0.8f), pos(0.5f, 0.5f, 1, 1)));
//        items.add(new TexturedQuadsItem(pos(-0.01f, -0.01f, 0.01f, 0.01f), pos(0, 0, 0.3f, 2f)));

        return this;
    }

    public void updateItems() {
//        if (items.size() < 1)
//            items.add(new TexturedQuadsItem(new Vector4f(-1, -0.5f, 0, 0), new Vector4f(0, 0, 1, 1)));
    }

    public void render() {
//        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
//        glEnable(GL_POLYGON_OFFSET_FILL);
//        glEnable(GL_PROGRAM_POINT_SIZE);
        glDisable(GL_DEPTH_TEST);
        updateItems();

        shaderProgram.activateShader();

        shaderProgram.updateUbo(0, mvp, pos);
        shaderProgram.loadVbo(items);

        shaderProgram.render();

        shaderProgram.deactivateShader();

    }


}
