package me.illgilp.worldeditglobalizer.common.scheduler;

import java.util.concurrent.ScheduledExecutorService;

public interface WegScheduler {

    static WegScheduler getInstance() {
        return WegSimpleScheduler.getInstance();
    }

    ScheduledExecutorService getAsyncExecutor();

    ScheduledExecutorService getSyncExecutor();

    ScheduledExecutorService getAsyncPacketWriteExecutor();

    ScheduledExecutorService getAsyncPacketReadExecutor();

    boolean isMainThread();

    void warnIfMainThread(String message);
}
