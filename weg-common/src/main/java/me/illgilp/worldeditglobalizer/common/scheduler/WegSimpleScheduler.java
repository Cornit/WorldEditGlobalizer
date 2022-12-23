package me.illgilp.worldeditglobalizer.common.scheduler;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import me.illgilp.worldeditglobalizer.common.IllegalInvocationException;
import org.jetbrains.annotations.NotNull;

public abstract class WegSimpleScheduler implements WegScheduler {

    private static WegSimpleScheduler INSTANCE;
    private final ScheduledExecutorService asyncExecutor = Executors.newSingleThreadScheduledExecutor(r ->
        buildThread(r, "WorldEditGlobalizer Async Thread"));
    private final ScheduledExecutorService synchronizationExecutor = Executors.newSingleThreadScheduledExecutor(r ->
        buildThread(r, "WorldEditGlobalizer Synchronization Thread"));
    private final ScheduledExecutorService syncExecutor = getNewSyncScheduledExecutorService();
    private final ScheduledExecutorService asyncPacketWriteExecutor = Executors.newSingleThreadScheduledExecutor(r ->
        buildThread(r, "WorldEditGlobalizer Async Packet Write Thread"));
    private final ScheduledExecutorService asyncPacketReadExecutor = Executors.newSingleThreadScheduledExecutor(r ->
        buildThread(r, "WorldEditGlobalizer Async Packet Read Thread"));


    private final Logger logger;

    public WegSimpleScheduler(Logger logger) {
        this.logger = logger;
        INSTANCE = this;
    }

    protected static WegSimpleScheduler getInstance() {
        return INSTANCE;
    }

    @NotNull
    private static Thread buildThread(Runnable r, String name) {
        final Thread thread = new Thread(r, name);
        thread.setDaemon(true);
        return thread;
    }

    public boolean shutdown() throws InterruptedException {
        asyncExecutor.shutdown();
        synchronizationExecutor.shutdown();
        asyncPacketWriteExecutor.shutdown();
        asyncPacketReadExecutor.shutdown();
        boolean[] results = {
            asyncExecutor.awaitTermination(5, TimeUnit.SECONDS),
            synchronizationExecutor.awaitTermination(5, TimeUnit.SECONDS),
            asyncPacketWriteExecutor.awaitTermination(5, TimeUnit.SECONDS),
            asyncPacketReadExecutor.awaitTermination(5, TimeUnit.SECONDS)
        };
        for (boolean result : results) {
            if (!result) {
                return false;
            }
        }
        return true;
    }

    public ScheduledExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }

    public ScheduledExecutorService getSyncExecutor() {
        return syncExecutor;
    }

    public ScheduledExecutorService getAsyncPacketWriteExecutor() {
        return asyncPacketWriteExecutor;
    }

    public ScheduledExecutorService getAsyncPacketReadExecutor() {
        return asyncPacketReadExecutor;
    }

    @Override
    public void warnIfMainThread(String message) {
        try {
            throw new ExecutionException(null);
        } catch (ExecutionException e) {
            logger.log(Level.WARNING, message, e);
        }
    }

    protected abstract SyncScheduledExecutorService getNewSyncScheduledExecutorService();

    protected abstract class SyncScheduledExecutorService implements ScheduledExecutorService {

        protected abstract Runnable wrap(Runnable runnable);

        protected abstract <V> Callable<V> wrap(Callable<V> callable);

        @NotNull
        @Override
        public ScheduledFuture<?> schedule(@NotNull Runnable command, long delay, @NotNull TimeUnit unit) {
            return synchronizationExecutor.schedule(wrap(command), delay, unit);
        }

        @NotNull
        @Override
        public <V> ScheduledFuture<V> schedule(@NotNull Callable<V> callable, long delay, @NotNull TimeUnit unit) {
            return synchronizationExecutor.schedule(wrap(callable), delay, unit);
        }

        @NotNull
        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(@NotNull Runnable command, long initialDelay, long period, @NotNull TimeUnit unit) {
            return synchronizationExecutor.scheduleAtFixedRate(wrap(command), initialDelay, period, unit);
        }

        @NotNull
        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(@NotNull Runnable command, long initialDelay, long delay, @NotNull TimeUnit unit) {
            return synchronizationExecutor.scheduleWithFixedDelay(wrap(command), initialDelay, delay, unit);
        }

        @Override
        public void shutdown() {
            throw new IllegalInvocationException("invoking shutdown of this executor is not allowed");
        }

        @NotNull
        @Override
        public List<Runnable> shutdownNow() {
            throw new IllegalInvocationException("invoking shutdownNow of this executor is not allowed");
        }

        @Override
        public boolean isShutdown() {
            return synchronizationExecutor.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return synchronizationExecutor.isTerminated();
        }

        @Override
        public boolean awaitTermination(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
            throw new IllegalInvocationException("invoking awaitTermination of this executor is not allowed");
        }

        @NotNull
        @Override
        public <T> Future<T> submit(@NotNull Callable<T> task) {
            return synchronizationExecutor.submit(wrap(task));
        }

        @NotNull
        @Override
        public <T> Future<T> submit(@NotNull Runnable task, T result) {
            return synchronizationExecutor.submit(wrap(task), result);
        }

        @NotNull
        @Override
        public Future<?> submit(@NotNull Runnable task) {
            return synchronizationExecutor.submit(wrap(task));
        }

        @NotNull
        @Override
        public <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks) throws InterruptedException {
            return synchronizationExecutor.invokeAll(tasks.stream().map(this::wrap).collect(Collectors.toList()));
        }

        @NotNull
        @Override
        public <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit) throws InterruptedException {
            return synchronizationExecutor.invokeAll(tasks.stream().map(this::wrap).collect(Collectors.toList()), timeout, unit);
        }

        @NotNull
        @Override
        public <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks) throws ExecutionException, InterruptedException {
            return synchronizationExecutor.invokeAny(tasks.stream().map(this::wrap).collect(Collectors.toList()));
        }

        @Override
        public <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return synchronizationExecutor.invokeAny(tasks.stream().map(this::wrap).collect(Collectors.toList()), timeout, unit);
        }

        @Override
        public void execute(@NotNull Runnable command) {
            synchronizationExecutor.execute(wrap(command));
        }
    }
}
