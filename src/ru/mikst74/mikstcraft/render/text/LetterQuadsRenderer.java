package ru.mikst74.mikstcraft.render.text;

import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import ru.mikst74.mikstcraft.model.font.Font;
import ru.mikst74.mikstcraft.model.font.Letter;
import ru.mikst74.mikstcraft.render.shader.font.LetterQuadsShaderProgram;
import ru.mikst74.mikstcraft.render.shader.texturedquads.TexturedQuadsItem;
import ru.mikst74.mikstcraft.texture.TextureLoader;

import java.util.ArrayList;
import java.util.List;

import static ru.mikst74.mikstcraft.render.shader.texturedquads.TexturedQuadsItem.color;
import static ru.mikst74.mikstcraft.render.shader.texturedquads.TexturedQuadsItem.pos;
import static org.lwjgl.opengl.GL11C.*;

@Getter
public class LetterQuadsRenderer {
    private final LetterQuadsShaderProgram shaderProgram;
    private       Matrix4f                 mvp;
    private       Font                     font;
    @Setter
    private       List<TexturedQuadsItem>  items;

    public LetterQuadsRenderer(Font font, int itemCount) {
        this.font          = font;
        this.shaderProgram = new LetterQuadsShaderProgram(itemCount);
        shaderProgram.init();
        shaderProgram.linkTexture(0, TextureLoader.loadTexture("wCvnX.png"));
        this.items = new ArrayList<>();
    }


    public LetterQuadsRenderer withMvp(Matrix4f mvp) {
        this.mvp = mvp;
        return this;
    }

    public LetterQuadsRenderer initialize() {
        shaderProgram.activateShader();
        shaderProgram.updateUbo(0, mvp);
        shaderProgram.deactivateShader();

        int colCount = 50;
        int rowCount = 20;
        float stepX = (float) 2 / colCount;
        float stepY = (float) 2 / rowCount;
        float letterSizeX = font.getLetterSize().x;
        float letterSizeY = font.getLetterSize().y;
        int i = 0;
        String s = "teSt!";
        for (Character c : s.toCharArray()) {
            Letter l = font.get(c);
            items.add(
                    new TexturedQuadsItem(pos(stepX * i, stepY, stepX * (i + 1), 0),
                            pos(letterSizeX * l.getX(), letterSizeY * l.getY(), letterSizeX * (l.getX() + 1), letterSizeY * (l.getY() + 1)),
                            color(1, 0, 0, 0.5f)));
            i++;
        }
        return this;
    }


    public void render() {
        glEnable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);

        shaderProgram.activateShader();

        shaderProgram.loadVbo(items);

        shaderProgram.render();

        shaderProgram.deactivateShader();

    }


}
