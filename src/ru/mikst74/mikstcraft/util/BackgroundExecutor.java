package ru.mikst74.mikstcraft.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Math.max;

/**
 * Created by Mikhail Krinitsyn on 09.01.2026
 */
public class BackgroundExecutor {
    /**
     * Used to offload compute-heavy tasks, such as chunk meshing and triangulation, from the render
     * thread to background threads.
     */
    public static final ExecutorService executorService = Executors.newFixedThreadPool(max(1, Runtime.getRuntime().availableProcessors() / 2), r -> {
        Thread t = new Thread(r);
        t.setPriority(Thread.MIN_PRIORITY);
        t.setName("Chunk builder");
        t.setDaemon(true);
        return t;
    });

    public static final Queue<DelayedRunnable> updateAndRenderRunnables = new ConcurrentLinkedQueue<>();

}
