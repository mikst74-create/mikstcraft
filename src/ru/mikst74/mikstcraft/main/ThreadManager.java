package ru.mikst74.mikstcraft.main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class ThreadManager {
    private final List<Thread>                   allThreads                  = new ArrayList<>();
    private final List<ScheduledExecutorService> allScheduledExecutorService = new ArrayList<>();

    public ThreadManager() {
    }

    public void stop() {
        allThreads.forEach(Thread::interrupt);
        allScheduledExecutorService.forEach(ScheduledExecutorService::shutdown);
    }

    public void addAndRunThread(Thread thread) {
        allThreads.add(thread);
        thread.start();
    }

    public void addScheduler(ScheduledExecutorService scheduler) {
        allScheduledExecutorService.add(scheduler);
    }
}
