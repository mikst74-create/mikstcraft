package ru.mikst74.mikstcraft.input;

import lombok.Getter;

import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;

public class KeyboardMap {
    @Getter
    private final InputHandlerMapType mapType;
    private final InputEvent[]        keyHandlers = new InputEvent[GLFW_KEY_LAST + 1];

    private final InputEventData inputEventData = new InputEventData();

    public KeyboardMap(InputHandlerMapType mapType) {
        this.mapType = mapType;
    }

    public void invokeKeyConsumer(int key, int action, int mods) {
        Consumer<InputEventData> handler = null;
        if (key >= 0) {
            InputEvent keyHandler = keyHandlers[key];
            if (action == GLFW_PRESS) {
                handler = keyHandler != null ? keyHandler.getRunnableOnPress() : null;
            }
            if (action == GLFW_RELEASE) {
                handler = keyHandler != null ? keyHandler.getRunnableOnRelease() : null;
            }
            if (handler != null) {
                inputEventData.i1 = key;
                inputEventData.i2 = action;
                inputEventData.i3 = mods;
                handler.accept(inputEventData);
            }
        }
    }

    public void addHandler(int key, InputEvent keyPressHandler) {
        keyHandlers[key] = keyPressHandler;
    }
}
