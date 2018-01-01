package me.illgilp.worldeditglobalizerbungee.listener;

import me.illgilp.worldeditglobalizerbungee.Callback;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.manager.MessageManager;
import me.illgilp.worldeditglobalizerbungee.network.PacketSender;
import me.illgilp.worldeditglobalizerbungee.network.packets.ClipboardSendPacket;
import me.illgilp.worldeditglobalizerbungee.network.packets.KeepAlivePacket;
import me.illgilp.worldeditglobalizerbungee.player.Player;
import me.illgilp.worldeditglobalizerbungee.runnables.KeepAliveRunnable;
import me.illgilp.worldeditglobalizerbungee.runnables.PacketRunnable;
import me.illgilp.worldeditglobalizerbungee.runnables.UserDataRunnable;
import me.illgilp.worldeditglobalizerbungee.util.ComponentUtils;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ServerConnectedListener implements Listener {

    @EventHandler
    public void onServerConnect(ServerConnectedEvent e){
        BungeeCord.getInstance().getScheduler().schedule(WorldEditGlobalizerBungee.getInstance(), new UserDataRunnable(e) {
            @Override
            public void run() {
                if(!hasUserData())return;
                ServerConnectedEvent e = (ServerConnectedEvent) getUserData();
                KeepAlivePacket ka = new KeepAlivePacket();
                Callback callback = new Callback(2000,ka.getIdentifier()) {
                    @Override
                    public void onTimeOut(Callback callback) {
                        if(!hasUserData())return;
                        ServerConnectedEvent e = (ServerConnectedEvent) getUserData();
                        Player.getPlayer(e.getPlayer()).setPluginOnCurrentServerInstalled(false);

                    }

                    @Override
                    public void onCallback(Callback callback, Object response) {
                        if(!hasUserData())return;
                        ServerConnectedEvent e = (ServerConnectedEvent) getUserData();
                        Player.getPlayer(e.getPlayer()).setPluginOnCurrentServerInstalled(true);
                        if(Player.getPlayer(e.getPlayer()).hasClipboard()) {
                            MessageManager.sendMessage(e.getPlayer(),"clipboard.start.downloading");
                            ClipboardSendPacket packet = new ClipboardSendPacket();
                            packet.setClipboardhash(Player.getPlayer(e.getPlayer()).getClipboard().getHash());
                            packet.setData(Player.getPlayer(e.getPlayer()).getClipboard().getData());
                            PacketSender.sendPacket(e.getPlayer(),packet);
                        }
                    }
                };
                callback.setUserData(e);
                callback.start();
                PacketSender.sendPacket(e.getPlayer(),ka);
                KeepAliveRunnable.start(e.getServer(),e.getPlayer());

            }
        },1000, TimeUnit.MILLISECONDS);

    }

}
