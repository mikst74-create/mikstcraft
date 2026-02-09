package ru.mikst74.mikstcraft.model;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;
import ru.mikst74.mikstcraft.collision.SelectionDetector;
import ru.mikst74.mikstcraft.dictionary.ItemDictionary;
import ru.mikst74.mikstcraft.input.InputEventData;
import ru.mikst74.mikstcraft.main.CommunicationManager;
import ru.mikst74.mikstcraft.model.coo.WorldCoo;
import ru.mikst74.mikstcraft.model.entity.BaseEntity;
import ru.mikst74.mikstcraft.model.item.BaseItem;
import ru.mikst74.mikstcraft.model.item.BlockItem;
import ru.mikst74.mikstcraft.settings.GameProperties;

import static ru.mikst74.mikstcraft.dictionary.BlockTypeDictionary.AIR_BLOCK;
import static ru.mikst74.mikstcraft.server.message.BlockServerMessage.createSetBlockMessage;
import static ru.mikst74.mikstcraft.settings.GameProperties.PLAYER_EYE_HEIGHT;

/**
 * Created by Mikhail Krinitsyn on 10.01.2026
 */
@Getter
public class Person extends BaseEntity {
    private final   Vector3f acceleration = new Vector3f(0, -30.0f, 0);
    protected final Vector3f eyePosition  = new Vector3f();

    private BaseItem primaryItem;
    private BaseItem secondaryItem;

    @Setter
    private SelectionDetector selectionDetector;

    public Person(CommunicationManager communicationManager) {
        super(communicationManager);
        position.x = 5;
        position.z=5;
        position.y = 3.3f;
        hitbox.assign(
                -GameProperties.PLAYER_WIDTH * 0.5f,
                0,
                -GameProperties.PLAYER_WIDTH * 0.5f,
                GameProperties.PLAYER_WIDTH * 1.0f,
                GameProperties.PLAYER_HEIGHT * 1.0f,
                GameProperties.PLAYER_WIDTH * 1.0f
        );
        normalSpeed = 4.0f;
        afterMove();
        afterRotate();
        primaryItem = ItemDictionary.getInstance().getAllItems().get(1);
    }

    @Override
    public void afterMove() {
        super.afterMove();
        eyePosition.set(position);
        eyePosition.y += PLAYER_EYE_HEIGHT;
        if (selectionDetector != null) {
            selectionDetector.determineSelectedVoxel(eyePosition, direction);
        }
    }

    @Override
    protected void afterRotate() {
        super.afterRotate();
        if (selectionDetector != null) {
            selectionDetector.determineSelectedVoxel(eyePosition, direction);
        }
    }

    public void doAttack(InputEventData inputEventData) {
        SelectedVoxel selectedVoxel = selectionDetector.getSelectedVoxel();
        if (selectedVoxel.isHasSelection()) {
            communicationManager.sendMessage(createSetBlockMessage(selectedVoxel.getCoo(), AIR_BLOCK));

        }
    }

    public void stopAttack(InputEventData inputEventData) {
    }

    public void stopInteraction(InputEventData inputEventData) {
    }

    public void doInteraction(InputEventData inputEventData) {
        SelectedVoxel selectedVoxel = selectionDetector.getSelectedVoxel();
        if (selectedVoxel.isHasSelection()) {
            if (primaryItem instanceof BlockItem) {
                WorldCoo wCoo = new WorldCoo(selectedVoxel.getCoo()).step(selectedVoxel.getSelectedFace());
                BlockItem blockItem = (BlockItem) primaryItem;
                communicationManager.sendMessage(createSetBlockMessage(wCoo, blockItem.getBlockTypeInfo()));
            }
        }
    }

}
