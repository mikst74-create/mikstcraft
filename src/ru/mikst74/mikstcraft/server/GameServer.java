package ru.mikst74.mikstcraft.server;

import ru.mikst74.mikstcraft.model.Person;
import ru.mikst74.mikstcraft.server.message.BlockServerMessage;
import ru.mikst74.mikstcraft.server.message.ServerMessage;
import ru.mikst74.mikstcraft.util.queue.ZeroCpuQueue;
import ru.mikst74.mikstcraft.world.WorldMap;

import java.util.ArrayList;
import java.util.List;

public class GameServer {
    private final WorldMap                    worldMap;
    private final List<Person>                players;
    private final ZeroCpuQueue<ServerMessage> queue;

    public GameServer(WorldMap worldMap) {
        this.worldMap = worldMap;

        players = new ArrayList<>();
        queue   = new ZeroCpuQueue<>();
        queue.addHandler(this::messageHandler);
        queue.startThread(100);
    }

    public void stop() {
        queue.stop();
    }

    public void joinPlayer(Person player) {
        players.add(player);
    }

    public void sendMessage(ServerMessage message) {
        queue.sendMessage(message);
    }

    private void messageHandler(ServerMessage message) {
        System.out.println("Got message:" + message);

        switch (message.getMessageType()) {
            case BLOCK_SET:
                BlockServerMessage bsm = (BlockServerMessage) message;
                worldMap.setVoxel(bsm.getBlockWorldCoo(), bsm.getBlockTypeInfo());
                break;
        }
    }
}
