package ru.mikst74.mikstcraft.render.shader.font;

import lombok.Getter;
import ru.mikst74.mikstcraft.render.shader.texturedquads.TexturedQuadsShaderProgram;
import ru.mikst74.mikstcraft.texture.TextureInfo;

@Getter
public class LetterQuadsShaderProgram extends TexturedQuadsShaderProgram {

    public LetterQuadsShaderProgram(int itemCount) {
        super(itemCount);
    }

    public void init() {
        createProgram();
        addVertexShader("text/letterquads.vs.glsl");
        addGeometryShader("text/letterquads.gs.glsl");
        addFragmentShader("text/letterquads.fs.glsl");
        linkProgram();
        addStd140Uniform("Uniforms", Float.BYTES * (16), false);
        addUniformTextureLocation("tex", TextureInfo.NO_TEXTURE);
        addVaoVbo();
        finish();

    }
}