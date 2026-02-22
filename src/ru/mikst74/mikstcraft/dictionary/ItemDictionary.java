package ru.mikst74.mikstcraft.dictionary;

import lombok.Getter;
import ru.mikst74.mikstcraft.model.item.BaseItem;
import ru.mikst74.mikstcraft.model.item.BlockItem;

import java.util.ArrayList;
import java.util.List;

public class ItemDictionary {

    public static final int            MAX_GROUPS = 256;
    @Getter
    private final       List<BaseItem> allItems;
    private final       List[]         itemsByGroup;

    private static final ItemDictionary INSTANCE = new ItemDictionary();

    // Public static method to provide a global point of access
    public static ItemDictionary getInstance() {
        return INSTANCE;
    }

    private ItemDictionary() {
        allItems     = new ArrayList<>();
        itemsByGroup = new ArrayList[MAX_GROUPS];
    }

    public void init() {
        BlockTypeDictionary.getInstance().getUsedIds().forEach(id ->
        {
            BlockTypeInfo blockTypeInfo = BlockTypeDictionary.getBlockTypeInfo(id);
            addItem(BlockItem.defaultStackableBlock(blockTypeInfo.getName(), blockTypeInfo.getFaceMaterial()[0].getTextureInfo(), blockTypeInfo));
        });
    }

    public void addItem(BaseItem baseItem) {
        allItems.add(baseItem);
        List<BaseItem> list = (List<BaseItem>) itemsByGroup[baseItem.getItemGroup().getGroupId()];
        if (list == null) {
            list                                               = new ArrayList<>();
            itemsByGroup[baseItem.getItemGroup().getGroupId()] = list;
        }

        list.add(baseItem);
    }

}
