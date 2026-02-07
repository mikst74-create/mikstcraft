package ru.mikst74.mikstcraft.util.time;

import static java.lang.Math.PI;
import static java.lang.Math.sin;

public class TimeUtil {
    private static final long start = System.currentTimeMillis();

    public static float elapsedTimeMs() {
        return System.currentTimeMillis() - start;
    }

    public static float frequency(float peekPerSecond) {
        return (float) sin(elapsedTimeMs() / PI * 0.02 * peekPerSecond);
    }

    public static float frequency(float peekPerSecond, float max) {
        return (float) frequency(peekPerSecond) * max;
    }

    public static float frequency01(float peekPerSecond) {
        return (float) (frequency(peekPerSecond) + 1.0f) * 0.5f;
    }

    public static float frequency01(float peekPerSecond, float max) {
        return (float) frequency01(peekPerSecond) * max;
    }
}
