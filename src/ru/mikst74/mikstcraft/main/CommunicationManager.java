package ru.mikst74.mikstcraft.main;

import ru.mikst74.mikstcraft.server.GameServer;
import ru.mikst74.mikstcraft.server.message.ServerMessage;

public class CommunicationManager {
    private final GameInstance gameInstance;
    private final GameServer   gameServer;

    public CommunicationManager(GameInstance gameInstance, GameServer gameServer) {
        this.gameInstance = gameInstance;
        this.gameServer   = gameServer;
    }

    public void sendMessage(ServerMessage serverMessage) {
        gameServer.sendMessage(serverMessage);
    }
}
