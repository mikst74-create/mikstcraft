package ru.mikst74.mikstcraft.input;

public class InputEventDictionary {
    public static InputEvent GO_FORWARD = new InputEvent("Идти вперед");
    public static InputEvent GO_BACK = new InputEvent("Идти назад");
    public static InputEvent GO_RIGHT = new InputEvent("Идти вправо");
    public static InputEvent GO_LEFT           = new InputEvent("Идти влево");
    public static InputEvent TOGGLE_SNEAK_MODE = new InputEvent("Красться");
    public static InputEvent TOGGLE_SQUAT_MODE = new InputEvent("Присесть");
    public static InputEvent JUMP              = new InputEvent("Прыгнуть");
    public static InputEvent FLY_UP = new InputEvent("Лететь вверх");
    public static InputEvent FLY_DOWN = new InputEvent("Лететь вниз");
    public static InputEvent ATTACK = new InputEvent("Атаковать/использовать инструмент");
    public static InputEvent INTERACTION = new InputEvent("Взаимодействовать с блоком/использовать предмет в руке");

    public static InputEvent SHOW_INVENTORY = new InputEvent("Октрыть инвентарь");
    public static InputEvent CLOSE_INVENTORY = new InputEvent("Закрыть инвентарь");
    public static InputEvent SHOW_MAP = new InputEvent("Октрыть карту");
    public static InputEvent CLOSE_MAP = new InputEvent("Закрыть карту");
    public static InputEvent SHOW_MENU = new InputEvent("Пауза/Октрыть меню");
    public static InputEvent CLOSE_MENU = new InputEvent("Продолжить/Закрыть меню");

    public static InputEvent QUIT = new InputEvent("Выйти из игры");
    public static InputEvent WIREFRAME = new InputEvent("wireframe");

}
