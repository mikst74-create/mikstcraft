package ru.mikst74.mikstcraft.model.time;

public class GameTick {

    public static final  float TICKS_PER_SECOND     = 500; // 60 ticks per second
    private static final float MILLISEC_IN_ONE_TICK = 1E+6f / TICKS_PER_SECOND; // 60 ticks per second
    private static       long  startFrom;
    private static       long  pauseFrom;

    private float lastTick;
    private float lastCurrentTick;
    private float lastDeltaTick;

    public GameTick() {
    }

    public static void start() {
        startFrom = System.currentTimeMillis();
    }

    public static void pause() {
        pauseFrom = System.currentTimeMillis();
    }

    public static void resume() {
        startFrom = System.currentTimeMillis() - pauseFrom;
        pauseFrom = 0;
    }

    public float getDeltaFromLastTick() {
        lastCurrentTick = getCurrentTick();
        lastDeltaTick   = lastCurrentTick - lastTick;
        lastTick        = lastCurrentTick;
        return lastDeltaTick;
    }

    public float getDeltaTick(float eventTick) {
        return getCurrentTick() - eventTick;
    }

    public static int getCurrentTick() {
        return (int) ((System.currentTimeMillis() - startFrom) / MILLISEC_IN_ONE_TICK);
    }
}
