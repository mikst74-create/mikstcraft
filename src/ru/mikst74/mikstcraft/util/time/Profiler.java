package ru.mikst74.mikstcraft.util.time;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.round;

public class Profiler {
    private static Map<String, Long> data = new HashMap<>();
    private static Map<String, Long> count = new HashMap<>();
    public static long start;
    private static long stop;

    public static void start() {
        start = System.nanoTime();
    }

    public static void stop() {
        stop = System.nanoTime();
    }

    public static void profile(String name, Runnable run) {
        long s = System.nanoTime();
        run.run();
        long e = System.nanoTime();
        data.put(name, data.getOrDefault(name, 0L) + (e - s));
        count.put(name, count.getOrDefault(name, 0L) + 1L);
    }

    public static void printProfile() {
        stop();
        long total = (stop - start);
        System.out.println("*************************************************************");
        System.out.println("** Profiler statistics: Total time = "+(total/1000000)+" ms");
        data.entrySet().stream()
                .forEach(es ->
                        System.out.println(es.getKey() + ": " + (es.getValue() / 1000000) + " ms " + ((float) round(((float) es.getValue() / (float)total) * 10000) / 100) + "%, "+count.get(es.getKey())+" calls, "+((float)round((float) (es.getValue() / 10000) /count.get(es.getKey())))/100+" ms per call"));
    }
}
