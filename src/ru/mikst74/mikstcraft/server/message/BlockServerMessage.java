package ru.mikst74.mikstcraft.server.message;

import lombok.Getter;
import ru.mikst74.mikstcraft.dictionary.BlockTypeInfo;
import ru.mikst74.mikstcraft.model.coo.WorldCoo;

@Getter
public class BlockServerMessage extends ServerMessage {
    private final WorldCoo      blockWorldCoo;
    private final BlockTypeInfo blockTypeInfo;

    public BlockServerMessage(ServerMessageType messageType, WorldCoo blockWorldCoo, BlockTypeInfo blockTypeInfo) {
        super(messageType);
        this.blockWorldCoo = blockWorldCoo;
        this.blockTypeInfo = blockTypeInfo;
    }

    public static BlockServerMessage createSetBlockMessage(WorldCoo blockWorldCoo, BlockTypeInfo blockTypeInfo)
    {
        return new BlockServerMessage(ServerMessageType.BLOCK_SET, blockWorldCoo, blockTypeInfo);
    }

    @Override
    public String toString() {
        return "BlockServerMessage{" +
                "gameTick=" + gameTick +
                ", messageType=" + messageType.name() +
                ", blockWorldCoo=" + blockWorldCoo +
                ", blockTypeInfo=" + blockTypeInfo +
                '}';
    }
}
