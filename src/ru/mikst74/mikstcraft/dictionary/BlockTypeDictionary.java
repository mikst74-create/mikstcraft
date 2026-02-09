package ru.mikst74.mikstcraft.dictionary;

import lombok.Getter;
import ru.mikst74.mikstcraft.model.FaceMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.mikst74.mikstcraft.dictionary.BlockTypeInfo.FULL_NO_SOLID_BLOCK;
import static ru.mikst74.mikstcraft.dictionary.BlockTypeInfo.FULL_SOLID_BLOCK;
import static ru.mikst74.mikstcraft.dictionary.TextureDictionary.DEFAULT_TEXTURE;

public class BlockTypeDictionary {
    public static BlockTypeInfo AIR_BLOCK;
    public static BlockTypeInfo DEFAULT_BLOCK;


    private static final int             MAX_BLOCK_TYPES = 256 * 256;
    @Getter
    private final        BlockTypeInfo[] allBlockTypes;
    private              int             nextId;
    @Getter
    private              List<Integer>   usedIds;

    private static final BlockTypeDictionary INSTANCE = new BlockTypeDictionary();

    // Public static method to provide a global point of access
    public static BlockTypeDictionary getInstance() {
        return INSTANCE;
    }

    private BlockTypeDictionary() {
        allBlockTypes = new BlockTypeInfo[MAX_BLOCK_TYPES];
        usedIds       = new ArrayList<>();

    }

    public void init() {
        AIR_BLOCK = new BlockTypeInfo(0, "Air", FULL_NO_SOLID_BLOCK, new FaceMaterial());
        uploadBlockType(AIR_BLOCK);
        DEFAULT_BLOCK = new BlockTypeInfo(1, "Default stone", FULL_SOLID_BLOCK, new FaceMaterial(DEFAULT_TEXTURE));
        uploadBlockType(DEFAULT_BLOCK);
        nextId = 2;
        AtomicInteger id = new AtomicInteger(nextId);
//        Arrays.asList(new FaceMaterial(rgb(0, 0, 0)), new FaceMaterial(rgb(46, 213, 64)), new FaceMaterial(rgb(255, 0, 0)), new FaceMaterial(rgb(255, 115, 0)), new FaceMaterial(rgb(252, 252, 0)), new FaceMaterial(rgb(0, 255, 255)), new FaceMaterial(rgb(63, 0, 255)), new FaceMaterial(rgb(76, 50, 176)), new FaceMaterial(rgb(2, 3, 3)), new FaceMaterial(rgb(255, 255, 255)), new FaceMaterial(rgb(42, 45, 46)), new FaceMaterial(rgb(157, 12, 205)), new FaceMaterial(rgb(71, 28, 19)), new FaceMaterial(rgb(30, 166, 154)), new FaceMaterial(rgb(8, 45, 61)), new FaceMaterial(rgb(11, 61, 3)), new FaceMaterial(rgb(75, 75, 124)), new FaceMaterial(rgb(28, 180, 133)))
//                .forEach((m) ->
//                        uploadBlockType(new BlockTypeInfo(id.getAndIncrement(), "bb" + id.get(), FULL_SOLID_BLOCK, m)));
        uploadBlockType(new BlockTypeInfo(id.getAndIncrement(), "bb" + id.get(), FULL_SOLID_BLOCK, new FaceMaterial(TextureDictionary.getTextureInfo("Grass"))));
        uploadBlockType(new BlockTypeInfo(id.getAndIncrement(), "bb" + id.get(), FULL_SOLID_BLOCK, new FaceMaterial(TextureDictionary.getTextureInfo("Bedrock"))));
//        uploadBlockType(new BlockTypeInfo(id.getAndIncrement(), "bb" + id.get(), FULL_SOLID_BLOCK, new FaceMaterial(TextureDictionary.getTextureInfo("Bricks"))));
        uploadBlockType(new BlockTypeInfo(id.getAndIncrement(), "bb" + id.get(), FULL_SOLID_BLOCK, new FaceMaterial(TextureDictionary.getTextureInfo("Basalt"))));
        uploadBlockType(new BlockTypeInfo(id.getAndIncrement(), "bb" + id.get(), FULL_SOLID_BLOCK, new FaceMaterial(TextureDictionary.getTextureInfo("Acacia"))));

        nextId = id.get();
    }

    public void uploadBlockType(BlockTypeInfo bti) {
        allBlockTypes[bti.getId()] = bti;
        usedIds.add((int) bti.getId());
        nextId = bti.getId() + 1;
    }

    public static BlockTypeInfo getBlockTypeInfo(int id) {
        BlockTypeInfo blockType = getInstance().allBlockTypes[id];
        return blockType == null ? DEFAULT_BLOCK : blockType;
    }

    public static BlockTypeInfo getByNum(int num) {
        return getInstance().allBlockTypes[getInstance().usedIds.get(num)];
    }

    public static int count() {
        return getInstance().usedIds.size();
    }
}
