package ru.mikst74.mikstcraft.input;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LAST;

public class KeyboardInputHandler {
    private final boolean[]                             keydown = new boolean[GLFW_KEY_LAST + 1];
    private final Map<InputHandlerMapType, KeyboardMap> keyboardMaps;
    private       KeyboardMap                           currentKeyboardMap;

    public KeyboardInputHandler() {
        keyboardMaps = new HashMap<>();
    }

    public void addKeyboardMap(KeyboardMap keyboardMap) {
        keyboardMaps.put(keyboardMap.getMapType(), keyboardMap);
    }

    public void selectActiveKeyboardMap(InputHandlerMapType mapType) {
        currentKeyboardMap = keyboardMaps.get(mapType);
    }


    public void onKey(long window, int key, int scancode, int action, int mods) {
        currentKeyboardMap.invokeKeyConsumer(key, action,    mods);
    }
}
