package me.illgilp.worldeditglobalizercommon.async;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AsyncScheduler {

    private final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private boolean running = false;
    private final Logger logger;

    public AsyncScheduler(Logger logger) {
        this.logger = logger;
    }


    public final void runAsync(Runnable runnable) {
        queue.add(runnable);
    }

    public final boolean start() {
        if (running) {
            return false;
        }
        running = true;
        while (running) {
            try {
                Runnable runnable = queue.take();
                try {
                    runnable.run();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Exception in AsyncRunnable: '" + runnable.getClass().getName() + "': ", e);
                }
            } catch (InterruptedException e) {
            }
        }
        return true;
    }

    public final boolean stop() {
        if (!running) {
            return false;
        }

        running = false;
        return true;
    }
}
