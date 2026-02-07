package ru.mikst74.mikstcraft.render.texturedquads;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import ru.mikst74.mikstcraft.render.shader.texturedquads.TexturedQuadsItem;
import ru.mikst74.mikstcraft.render.shader.texturedquads.TexturedQuadsShaderProgram;
import ru.mikst74.mikstcraft.settings.GameProperties;
import ru.mikst74.mikstcraft.texture.TextureLoader;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.toRadians;
import static ru.mikst74.mikstcraft.render.shader.texturedquads.TexturedQuadsItem.color;
import static ru.mikst74.mikstcraft.render.shader.texturedquads.TexturedQuadsItem.pos;
import static ru.mikst74.mikstcraft.util.time.TimeUtil.frequency;
import static org.lwjgl.opengl.GL11C.*;

public class TexturedQuadsRendererFun {
    private final TexturedQuadsShaderProgram texturedQuadsShaderProgram;
    private       Matrix4f                   mvp;
    private       List<TexturedQuadsItem>    items;

    public TexturedQuadsRendererFun() {
        this.texturedQuadsShaderProgram = new TexturedQuadsShaderProgram(2000);
        texturedQuadsShaderProgram.init();
        texturedQuadsShaderProgram.linkTexture(0, TextureLoader.loadTexture("interface-menu-pixel-buttons-retro-video-game-ui-vector-52830508.png"));
        this.items = new ArrayList<>();
    }

    public TexturedQuadsRendererFun withMvp(Matrix4f mvp) {
        this.mvp = mvp;
        return this;
    }

    public TexturedQuadsRendererFun initialize() {
        texturedQuadsShaderProgram.activateShader();
        texturedQuadsShaderProgram.updateUbo(0, mvp);
        texturedQuadsShaderProgram.deactivateShader();

        items.add(new TexturedQuadsItem(pos(-1f, 1f, 1f, -1f),
                pos(0, 0, 1, 1),
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

        texturedQuadsShaderProgram.activateShader();
        Matrix4f tmpMat = new Matrix4f()
                .ortho(-1, 1, -1, 1, 1, 10)
//                .setPerspective((float) toRadians(72), 1.2f,-30,50);
                ;
        Matrix4f m2 = new Matrix4f()
//                .translate(0,0,30)
                .perspective((float) toRadians(52), 1.4f, 1, 10)

//                .perspective((float) toRadians(15), 0.8f,-1,100)
                .translate(0, 0, 10)
//                .perspective(-1,1,-1,1,-1,1)
//                .setPerspective((float) toRadians(72), 1.2f,-30,50)
                ;

//                .scale(0.5f)
//                .translate(0.03f,0.03f,0.03f)
        ;
        Quaternionf tmpq = new Quaternionf();

        Matrix4f vMat = new Matrix4f();
        Matrix4f pMat = new Matrix4f();
        vMat.rotation(tmpq.rotationX(-0.03f).rotateY(frequency(0.5f, 0.01f)).rotateLocalZ(frequency(0.8f, 0.01f)));
        vMat.translate(0, 0, -2);
//        vMat.translate(0, 0, frequency(0.4f,1));
//        vMat.rotation(tmpq.rotationX(frequency(0.4f,1)).rotateY(0).rotateLocalZ(0));

        pMat.setPerspective((float) toRadians(GameProperties.FOV_DEGREES), 1.2f, 1, 10);
        pMat.mulPerspectiveAffine(vMat, tmpMat);

//        System.out.println("m2:vec000:"+new Vector4f(0,0,0.1f,1).mul(m2));
//        System.out.println("tm:vec000:"+new Vector4f(0,0,0.1f,1).mul(tmpMat));
//        System.out.println("m2:vec110:"+new Vector4f(1,1,1f,1).mul(m2));
//        System.out.println("tm:vec110:"+new Vector4f(1,1,1f,1).mul(tmpMat));
        Matrix4f r = new Matrix4f()
//        .ortho(-1,1,-1,1,0,1)
                ;
//        tmpMat.mulPerspectiveAffine(m2,r);
        texturedQuadsShaderProgram.updateUbo(0, tmpMat);
        texturedQuadsShaderProgram.loadVbo(items);

        texturedQuadsShaderProgram.render();

        texturedQuadsShaderProgram.deactivateShader();

    }


}
