package me.illgilp.worldeditglobalizer.server.bukkit.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import me.illgilp.worldeditglobalizer.common.scheduler.WegSimpleScheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;


public class WegSimpleSchedulerImpl extends WegSimpleScheduler {

    private final Plugin plugin;

    public WegSimpleSchedulerImpl(Plugin plugin) {
        super(plugin.getLogger());
        this.plugin = plugin;
    }

    @Override
    protected SyncScheduledExecutorService getNewSyncScheduledExecutorService() {
        return new SyncScheduledExecutorService() {
            @Override
            protected Runnable wrap(Runnable runnable) {
                return () ->
                {
                    try {
                        Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                            runnable.run();
                            return null;
                        }).get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                };
            }

            @Override
            protected <V> Callable<V> wrap(Callable<V> callable) {
                return () -> Bukkit.getScheduler().callSyncMethod(plugin, callable).get();
            }
        };
    }

    @Override
    public boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }
}
