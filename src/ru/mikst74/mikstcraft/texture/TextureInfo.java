package ru.mikst74.mikstcraft.texture;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2i;

@Setter
@Getter
@Builder
public class TextureInfo {
    public static final TextureInfo NO_TEXTURE = TextureInfo.builder().build();

    private String   name;
    private Vector2i size;
    private String   fileName;
    private int      textureId;
    private int      textureArrayId;
    private boolean  isArray;

}
