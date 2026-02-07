package ru.mikst74.mikstcraft.server.message;

import lombok.Getter;

import javax.swing.text.html.parser.Entity;

@Getter
public class EntityServerMessage extends ServerMessage {
    private final Entity entity;

    public EntityServerMessage(ServerMessageType messageType, Entity entity) {
        super(messageType);
        this.entity = entity;
    }

    @Override
    public String toString() {
        return "EntityServerMessage{" +
                "gameTick=" + gameTick +
                ", messageType=" + messageType +
                ", entity=" + entity +
                '}';
    }
}
