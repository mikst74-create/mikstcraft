package ru.mikst74.mikstcraft.input;

import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

public class InputEvent {
    @Getter
    String                   name;
    @Getter
    @Setter
    Consumer<InputEventData> runnableOnPress;
    @Getter
    @Setter
    Consumer<InputEventData>                 runnableOnRelease;

    public InputEvent(String name) {
        this.name              = name;
        this.runnableOnPress   = null;
        this.runnableOnRelease = null;
    }

    public void setRunnable(Consumer<InputEventData> runnableOnPress, Consumer<InputEventData> runnableOnRelease) {
        this.runnableOnPress   = runnableOnPress;
        this.runnableOnRelease = runnableOnRelease;
    }
}
