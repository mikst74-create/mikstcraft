package ru.mikst74.mikstcraft.model.item;

import lombok.Getter;
import ru.mikst74.mikstcraft.texture.TextureInfo;

@Getter
public class BaseItem {
    private final String name;
    private final ItemGroup itemGroup;
    private final boolean isStackable;
    private final int maxInStack;
    private final TextureInfo textureInfo;

    public BaseItem(String name, ItemGroup itemGroup, boolean isStackable, int maxInStack, TextureInfo textureInfo) {
        this.name        = name;
        this.itemGroup   = itemGroup;
        this.isStackable = isStackable;
        this.maxInStack  = maxInStack;
        this.textureInfo = textureInfo;
    }
}
