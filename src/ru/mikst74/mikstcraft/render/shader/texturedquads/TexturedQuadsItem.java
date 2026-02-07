package ru.mikst74.mikstcraft.render.shader.texturedquads;

import lombok.Getter;
import org.joml.Vector4f;

@Getter
public class TexturedQuadsItem {
    private Vector4f quadPos;
    private Vector4f texPos;
    private Vector4f colorOverlay;

    public TexturedQuadsItem() {
        this(new Vector4f(), new Vector4f(), new Vector4f());
    }

    public TexturedQuadsItem(Vector4f quadPos, Vector4f texPos, Vector4f colorOverlay) {
        this.quadPos = quadPos;
        this.texPos = texPos;
        this.colorOverlay = colorOverlay;
    }

    public static Vector4f pos(float left, float top, float right, float bottom) {
        return new Vector4f(left, top, right, bottom);
    }

    public static Vector4f color(float red, float green, float blue, float alpha) {
        return new Vector4f(red, green, blue, alpha);
    }
}
