package ru.mikst74.mikstcraft.input;

import ru.mikst74.mikstcraft.main.Starter;

import static ru.mikst74.mikstcraft.input.InputEventDictionary.*;
import static org.lwjgl.glfw.GLFW.*;

public class DefaultMapCreator {
    public static KeyboardMap createDefaultPlayerWorldKeyboardMap(Starter starter) {
        KeyboardMap map = new KeyboardMap(InputHandlerMapType.PLAYER_WORLD);
        map.addHandler(GLFW_KEY_ESCAPE, QUIT);
        map.addHandler(GLFW_KEY_L, WIREFRAME);
//        starter::stop/
//        map.addHandler(GLFW_KEY, ,null);
        map.addHandler(GLFW_KEY_W, GO_FORWARD);
        map.addHandler(GLFW_KEY_D, GO_RIGHT);
        map.addHandler(GLFW_KEY_A, GO_LEFT);
        map.addHandler(GLFW_KEY_S, GO_BACK);
        map.addHandler(GLFW_KEY_Y, FLY_UP);
        map.addHandler(GLFW_KEY_H, FLY_DOWN);
        map.addHandler(GLFW_KEY_C, TOGGLE_SQUAT_MODE);
        map.addHandler(GLFW_KEY_LEFT_SHIFT, TOGGLE_SNEAK_MODE);
        map.addHandler(GLFW_KEY_SPACE, JUMP);

        return map;
    }

    public static MouseMap createDefaultPlayerWorldMouseMap(Starter starter) {
        MouseMap map = new MouseMap(InputHandlerMapType.PLAYER_WORLD);
        map.addHandler(GLFW_MOUSE_BUTTON_1, ATTACK);
        map.addHandler(GLFW_MOUSE_BUTTON_2, INTERACTION);

        return map;
    }
}
