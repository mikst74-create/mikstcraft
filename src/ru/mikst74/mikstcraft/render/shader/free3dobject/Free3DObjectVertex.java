package ru.mikst74.mikstcraft.render.shader.free3dobject;

import lombok.Getter;
import org.joml.Vector4f;

@Getter
public class Free3DObjectVertex {
    private Vector4f pos;
    private Vector4f texPos;
    private Vector4f colorOverlay;

    public Free3DObjectVertex() {
        this(new Vector4f(), new Vector4f(), new Vector4f());
    }

    public Free3DObjectVertex(Vector4f pos, Vector4f texPos, Vector4f colorOverlay) {
        this.pos          = pos;
        this.texPos       = texPos;
        this.colorOverlay = colorOverlay;
    }

    public static Vector4f color(float red, float green, float blue, float alpha) {
        return new Vector4f(red, green, blue, alpha);
    }
}
