package ru.mikst74.mikstcraft.util;

import java.util.concurrent.Callable;

/**
 * An action that is optionally delayed a given number of frames.
 */
public class DelayedRunnable {
    public final Callable<Void> runnable;
    public final String name;
    public int delay;

    public DelayedRunnable(Callable<Void> runnable, String name, int delay) {
        this.runnable = runnable;
        this.name = name;
        this.delay = delay;
    }
}
