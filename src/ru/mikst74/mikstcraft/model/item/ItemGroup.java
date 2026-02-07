package ru.mikst74.mikstcraft.model.item;

import lombok.Getter;

@Getter
public class ItemGroup {
    private final int groupId;
    private final String groupName;

    public ItemGroup(int groupId, String groupName) {
        this.groupId   = groupId;
        this.groupName = groupName;
    }
}
