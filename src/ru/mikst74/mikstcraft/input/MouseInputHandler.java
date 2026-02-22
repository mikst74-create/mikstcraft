package ru.mikst74.mikstcraft.input;

import lombok.Setter;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static java.lang.Math.toRadians;

public class MouseInputHandler {
    private final Map<InputHandlerMapType, MouseMap> mouseMaps;
    private       MouseMap                           currentMouseMap;


    // Handle mouse move
    private boolean firstCursorPos;
    private double  lastX;
    private double  lastY;
    double sensitivity = 0.05;

    private final Vector3f mouseMoveVector   = new Vector3f();
    private final Vector2f mouseScrollVector = new Vector2f();
    private final Vector3i mouseButtonVector = new Vector3i();

    @Setter
    private Consumer<InputEventData> mouseMoveConsumer;
    @Setter
    private Consumer<Vector2f>       mouseScrollConsumer;


    private final InputEventData inputEventData;

    public MouseInputHandler() {
        this.mouseMaps = new HashMap<>();

        inputEventData = new InputEventData();
    }

    public void addMouseMap(MouseMap mouseMap) {
        mouseMaps.put(mouseMap.getMapType(), mouseMap);
    }

    public void selectActiveMouseMap(InputHandlerMapType mapType) {
        currentMouseMap = mouseMaps.get(mapType);
    }

    public void onCursorPos(long window, double xpos, double ypos) {
        if (!firstCursorPos) {
            // Calculate how far the mouse moved since the last frame
            double dx = (xpos - lastX);
            double dy = (ypos - lastY); // Reversed: y-coords go from bottom to top
            lastX = xpos;
            lastY = ypos;

            // Apply sensitivity
            mouseMoveVector.x = (float) toRadians(dx * sensitivity);
            mouseMoveVector.y = (float) toRadians(dy * sensitivity);

        }
        firstCursorPos = false;
        lastX          = xpos;
        lastY          = ypos;

        if (mouseMoveConsumer != null) {
            inputEventData.f1 = mouseMoveVector.x;
            inputEventData.f2 = mouseMoveVector.y;
            inputEventData.f3 = mouseMoveVector.z;
            mouseMoveConsumer.accept(inputEventData);
        }
    }

    public void onScroll(long window, double xoffset, double yoffset) {
        if (mouseScrollConsumer != null) {
            mouseScrollVector.x = (float) xoffset;
            mouseScrollVector.y = (float) yoffset;
            mouseScrollConsumer.accept(mouseScrollVector);
        }
    }

    /**
     * GLFW callback for mouse buttons.
     */
    public void onMouseButton(long window, int button, int action, int mods) {
        currentMouseMap.invokeKeyConsumer(button, action, mods);
    }
}
