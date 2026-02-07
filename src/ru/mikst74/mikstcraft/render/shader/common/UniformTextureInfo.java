package ru.mikst74.mikstcraft.render.shader.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.mikst74.mikstcraft.texture.TextureInfo;

@AllArgsConstructor
@Getter
public class UniformTextureInfo {
    private int textureLocId;
    private int unitId;
    private int textureType;
    @Setter
    private TextureInfo textureInfo;


}
