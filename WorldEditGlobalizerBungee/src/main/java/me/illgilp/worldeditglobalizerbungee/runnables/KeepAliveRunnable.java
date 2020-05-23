package me.illgilp.worldeditglobalizerbungee.runnables;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.callback.Callback;
import me.illgilp.worldeditglobalizerbungee.network.PacketSender;
import me.illgilp.worldeditglobalizerbungee.player.Player;
import me.illgilp.worldeditglobalizerbungee.player.ServerUsability;
import me.illgilp.worldeditglobalizercommon.network.packets.KeepAlivePacket;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class KeepAliveRunnable implements Runnable {

    private static final Map<UUID, KeepAliveRunnable> keepAliveRunnables = new HashMap<>();

    private Server server;
    private Player player;
    private ScheduledTask task;

    private KeepAliveRunnable(Server server, Player player) {
        this.server = server;
        this.player = player;
    }

    public static void start(Server server, Player player) {
        if (keepAliveRunnables.containsKey(player.getUniqueId())) {
            keepAliveRunnables.get(player.getUniqueId()).task.cancel();
        }
        KeepAliveRunnable runnable = new KeepAliveRunnable(server, player);
        runnable.setTask(BungeeCord.getInstance().getScheduler().schedule(WorldEditGlobalizerBungee.getInstance(), runnable, 1000, 1000, TimeUnit.MILLISECONDS));
        keepAliveRunnables.put(player.getUniqueId(), runnable);
    }

    @Override
    public void run() {
        if (!player.getProxiedPlayer().isConnected() || player.getProxiedPlayer().getServer() == null) {
            task.cancel();
            return;
        }
        if (server.hashCode() == player.getProxiedPlayer().getServer().hashCode()) {
            KeepAlivePacket packet = new KeepAlivePacket(WorldEditGlobalizerBungee.getInstance().getDescription().getVersion());
            Callback callback = new Callback(750, packet.getIdentifier()) {
                @Override
                public void onTimeOut(Callback callback) {
                    if (player.getServerUsability() == ServerUsability.KEY_NOT_CORRECT) {
                        return;
                    }
                    task.cancel();
                    player.setServerUsability(ServerUsability.PLUGIN_NOT_INSTALLED);
                    PingRunnable.start(server, player);
                }

                @Override
                public void onCallback(Callback callback, Object response) {
                    if (response instanceof KeepAlivePacket) {
                        if (!((KeepAlivePacket) response).getVersion().equals(WorldEditGlobalizerBungee.getInstance().getDescription().getVersion())) {
                            player.setServerVersion(((KeepAlivePacket) response).getVersion());
                            player.setServerUsability(ServerUsability.INCOMPATIBLE_VERSION);
                            return;
                        }
                        player.setServerUsability(ServerUsability.KEY_CORRECT);
                    }
                }
            };
            PacketSender.sendPacket(player, packet);
            callback.start();
        } else {
            task.cancel();
        }
    }

    public void setTask(ScheduledTask task) {
        this.task = task;
    }

}
