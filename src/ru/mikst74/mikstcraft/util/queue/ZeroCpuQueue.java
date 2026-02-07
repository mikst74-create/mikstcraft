package ru.mikst74.mikstcraft.util.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ZeroCpuQueue<T> {
    private BlockingQueue<T>  queue;
    private List<Consumer<T>> messageHandler;
    private Thread            consumer;

    public ZeroCpuQueue() {
        this.messageHandler = new ArrayList<>();
    }

    public void addHandler(Consumer<T> messageHandler) {
        this.messageHandler.add(messageHandler);
    }

    public void startThread(int capacity) {
        queue = new ArrayBlockingQueue<>(capacity);
        //  Consumer Thread (Zero CPU while waiting)
        consumer = new Thread(() -> {
            try {
                while (true) {
//                    System.out.println("Consumer waiting...");
                    // take() blocks effectively using OS-level wait()
                    T task = queue.poll(1000, TimeUnit.MILLISECONDS);
                    if (task != null) {
                        messageHandler.forEach(i -> i.accept(task));
//                        System.out.println("Processed: " + task);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        consumer.start();
    }

    public void stop() {
        consumer.interrupt();
    }

    public void sendMessage(T message) {
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
