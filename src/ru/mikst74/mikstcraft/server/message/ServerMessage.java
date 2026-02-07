package ru.mikst74.mikstcraft.server.message;

import lombok.Getter;
import ru.mikst74.mikstcraft.model.time.GameTick;

@Getter
public abstract class ServerMessage {
    protected final int gameTick;
    protected final ServerMessageType messageType;

    public ServerMessage(ServerMessageType messageType) {
        gameTick=GameTick.getCurrentTick();
        this.messageType = messageType;
    }
}
