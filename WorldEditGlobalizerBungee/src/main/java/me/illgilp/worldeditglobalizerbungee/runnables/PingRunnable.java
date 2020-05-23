package me.illgilp.worldeditglobalizerbungee.runnables;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.callback.Callback;
import me.illgilp.worldeditglobalizerbungee.callback.PingCallback;
import me.illgilp.worldeditglobalizerbungee.manager.PlayerManager;
import me.illgilp.worldeditglobalizerbungee.player.Player;
import me.illgilp.worldeditglobalizerbungee.player.ServerUsability;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class PingRunnable implements Runnable {

    private static final Map<UUID, PingRunnable> pingRunnables = new HashMap<>();

    private Server server;
    private Player player;
    private ScheduledTask task;

    private PingRunnable(Server server, Player player) {
        this.server = server;
        this.player = player;
    }

    public static void start(Server server, Player player) {
        if (pingRunnables.containsKey(player.getUniqueId())) {
            pingRunnables.get(player.getUniqueId()).task.cancel();
        }
        PingRunnable runnable = new PingRunnable(server, player);
        runnable.setTask(BungeeCord.getInstance().getScheduler().schedule(WorldEditGlobalizerBungee.getInstance(), runnable, 1000, 1000, TimeUnit.MILLISECONDS));
        pingRunnables.put(player.getUniqueId(), runnable);
    }

    @Override
    public void run() {
        if (!player.getProxiedPlayer().isConnected() || player.getProxiedPlayer().getServer() == null) {
            task.cancel();
            return;
        }
        if (server.hashCode() == player.getProxiedPlayer().getServer().hashCode()) {
            PingCallback callback = new PingCallback<Object>(750, player) {
                @Override
                public void onTimeOut(Callback callback) {
                    if (PlayerManager.getInstance().getPlayer(player.getUniqueId()) != null) {
                        PlayerManager.getInstance().getPlayer(player.getUniqueId()).setServerUsability(ServerUsability.PLUGIN_NOT_INSTALLED);
                    }
                }

                @Override
                public void onCallback(Callback callback, Boolean response) {
                    if (!response) {
                        player.setServerUsability(ServerUsability.KEY_NOT_SET);
                        return;
                    }
                    KeepAliveRunnable.start(server, player);
                }
            };
            callback.start();
        } else {
            task.cancel();
        }
    }

    public void setTask(ScheduledTask task) {
        this.task = task;
    }

}
