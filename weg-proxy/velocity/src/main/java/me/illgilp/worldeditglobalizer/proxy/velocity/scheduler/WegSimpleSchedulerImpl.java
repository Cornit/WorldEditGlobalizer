package me.illgilp.worldeditglobalizer.proxy.velocity.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import me.illgilp.worldeditglobalizer.common.scheduler.WegSimpleScheduler;
import me.illgilp.worldeditglobalizer.proxy.velocity.WorldEditGlobalizerPlugin;


public class WegSimpleSchedulerImpl extends WegSimpleScheduler {

    private final WorldEditGlobalizerPlugin plugin;

    public WegSimpleSchedulerImpl(WorldEditGlobalizerPlugin plugin) {
        super(plugin.getLogger());
        this.plugin = plugin;
    }

    @Override
    protected SyncScheduledExecutorService getNewSyncScheduledExecutorService() {
        return new SyncScheduledExecutorService() {
            @Override
            protected Runnable wrap(Runnable runnable) {
                return () -> plugin.getProxyServer().getScheduler().buildTask(plugin, runnable)
                    .schedule();
            }

            @Override
            protected <V> Callable<V> wrap(Callable<V> callable) {
                return () -> {
                    CompletableFuture<V> future = new CompletableFuture<>();
                    plugin.getProxyServer().getScheduler().buildTask(plugin, () -> {
                        try {
                            future.complete(callable.call());
                        } catch (Exception e) {
                            future.completeExceptionally(e);
                        }
                    });
                    return future.get();
                };
            }
        };
    }

    @Override
    public boolean isMainThread() {
        return false;
    }
}
