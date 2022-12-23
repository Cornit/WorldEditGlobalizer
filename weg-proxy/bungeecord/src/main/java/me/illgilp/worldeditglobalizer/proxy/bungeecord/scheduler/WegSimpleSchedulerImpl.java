package me.illgilp.worldeditglobalizer.proxy.bungeecord.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import me.illgilp.worldeditglobalizer.common.scheduler.WegSimpleScheduler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;


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
                ExecutorService service = ProxyServer.getInstance().getScheduler().unsafe().getExecutorService(plugin);
                return () -> {
                    try {
                        service.submit(runnable).get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                };
            }

            @Override
            protected <V> Callable<V> wrap(Callable<V> callable) {
                ExecutorService service = ProxyServer.getInstance().getScheduler().unsafe().getExecutorService(plugin);
                return () -> {
                    try {
                        return service.submit(callable).get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                };
            }
        };
    }

    @Override
    public boolean isMainThread() {
        return false;
    }
}
