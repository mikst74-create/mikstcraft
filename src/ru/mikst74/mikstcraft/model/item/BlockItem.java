package ru.mikst74.mikstcraft.model.item;

import lombok.Getter;
import ru.mikst74.mikstcraft.dictionary.BlockTypeInfo;
import ru.mikst74.mikstcraft.texture.TextureInfo;

public class BlockItem extends BaseItem {
    public static final int DEFAULT_IN_STACK = 99;
    public static ItemGroup GROUP            = new ItemGroup(1, "Блок");
    @Getter
    private       BlockTypeInfo blockTypeInfo;

    public BlockItem(String name, boolean isStackable, int maxInStack, TextureInfo textureInfo, BlockTypeInfo blockTypeInfo) {
        super(name, GROUP, isStackable, maxInStack, textureInfo);
        this.blockTypeInfo = blockTypeInfo;
    }

    public static BlockItem defaultStackableBlock(String name, TextureInfo textureInfo, BlockTypeInfo blockTypeInfo) {
        BlockItem blockItem = new BlockItem(name, true, DEFAULT_IN_STACK, textureInfo, blockTypeInfo);
        return blockItem;
    }
}
