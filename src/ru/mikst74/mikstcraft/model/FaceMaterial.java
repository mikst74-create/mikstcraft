package ru.mikst74.mikstcraft.model;

import lombok.Getter;
import ru.mikst74.mikstcraft.texture.TextureInfo;

import static ru.mikst74.mikstcraft.texture.TextureInfo.NO_TEXTURE;

/**
 * Simple material definition with only a color.
 */
@Getter
public class FaceMaterial {
    private final TextureInfo textureInfo;

    public FaceMaterial(TextureInfo textureInfo) {
        this.textureInfo = textureInfo;
    }

    public FaceMaterial() {
        this.textureInfo = NO_TEXTURE;
    }
}
