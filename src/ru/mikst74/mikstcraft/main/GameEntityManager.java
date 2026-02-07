package ru.mikst74.mikstcraft.main;

public class GameEntityManager {
    private final GameInstance gameInstance;

    public GameEntityManager(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public void gameTickHandler(Long aLong) {
//        System.out.println("tick " + aLong);
        gameInstance.getPlayers().forEach(h->h.applyGameTick(aLong));
    }


    public Thread createThread() {
        Thread renderThread = new Thread(this::mainLoop);
        renderThread.setName("GameEntityManager Thread");
        renderThread.setPriority(Thread.MAX_PRIORITY);
        return renderThread;
    }


    private void mainLoop() {
        while (!Thread.currentThread().isInterrupted()) {

        }
    }
}
