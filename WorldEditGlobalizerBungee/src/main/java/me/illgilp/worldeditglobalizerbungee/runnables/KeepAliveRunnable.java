package me.illgilp.worldeditglobalizerbungee.runnables;

import me.illgilp.worldeditglobalizerbungee.Callback;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.network.PacketSender;
import me.illgilp.worldeditglobalizerbungee.network.packets.KeepAlivePacket;
import me.illgilp.worldeditglobalizerbungee.player.Player;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;

public class KeepAliveRunnable implements Runnable {


    private Server server;
    private ProxiedPlayer player;
    private ScheduledTask task;

    private KeepAliveRunnable(Server server, ProxiedPlayer player) {
        this.server = server;
        this.player = player;
    }

    @Override
    public void run() {
        if(!player.isConnected() || player.getServer() == null){task.cancel();return;}
        if (server.hashCode() == player.getServer().hashCode()) {
            KeepAlivePacket packet = new KeepAlivePacket();
            Callback callback = new Callback(750,packet.getIdentifier()) {
                @Override
                public void onTimeOut(Callback callback) {
                    Player.getPlayer(player).setPluginOnCurrentServerInstalled(false);
                }
                @Override
                public void onCallback(Callback callback, Object response) {
                    Player.getPlayer(player).setPluginOnCurrentServerInstalled(true);
                }
            };
            PacketSender.sendPacket(player,packet);
            callback.start();
        }else {
            task.cancel();
        }
    }

    public void setTask(ScheduledTask task) {
        this.task = task;
    }

    public static void start(Server server, ProxiedPlayer player){
        KeepAliveRunnable runnable = new KeepAliveRunnable(server,player);
        runnable.setTask(BungeeCord.getInstance().getScheduler().schedule(WorldEditGlobalizerBungee.getInstance(),runnable,1000,1000, TimeUnit.MILLISECONDS));
    }

}
