package ru.mikst74.mikstcraft.input;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;

public class MouseMap {
    private final InputEvent[] buttonHandlers = new InputEvent[GLFW_MOUSE_BUTTON_LAST + 1];

    @Getter
    private final InputHandlerMapType mapType;
    @Setter
    @Getter
    private       Consumer<Vector3f>  mouseMoveHandler;
    @Setter
    @Getter
    private       Consumer<Vector2f>  mouseScrollHandler;

    private InputEventData inputEventData = new InputEventData();

    public MouseMap(InputHandlerMapType mapType) {
        this.mapType = mapType;
    }

    public void invokeKeyConsumer(int button, int action, int mods) {
        Consumer<InputEventData> handler = null;
        if (button >= 0) {
            InputEvent buttonHandler = buttonHandlers[button];
            if (action == GLFW_PRESS) {
                handler = buttonHandler != null ? buttonHandler.getRunnableOnPress() : null;
            }
            if (action == GLFW_RELEASE) {
                handler = buttonHandler != null ? buttonHandler.getRunnableOnRelease() : null;
            }
            if (handler != null) {
                inputEventData.i1 = button;
                inputEventData.i2 = action;
                inputEventData.i3 = mods;
                handler.accept(inputEventData);
            }
        }
    }

    public void addHandler(int button, InputEvent keyPressHandler) {
        buttonHandlers[button] = keyPressHandler;
    }
}
