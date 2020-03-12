package me.illgilp.worldeditglobalizerbungee.listener;

import java.util.concurrent.TimeUnit;
import me.illgilp.worldeditglobalizerbungee.Callback;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.manager.MessageManager;
import me.illgilp.worldeditglobalizerbungee.network.PacketSender;
import me.illgilp.worldeditglobalizerbungee.player.Player;
import me.illgilp.worldeditglobalizerbungee.runnables.KeepAliveRunnable;
import me.illgilp.worldeditglobalizerbungee.runnables.UserDataRunnable;
import me.illgilp.worldeditglobalizercommon.network.packets.ClipboardSendPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.KeepAlivePacket;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerConnectedListener implements Listener {

    @EventHandler
    public void onServerConnect(ServerConnectedEvent e) {
        BungeeCord.getInstance().getScheduler().schedule(WorldEditGlobalizerBungee.getInstance(), new UserDataRunnable(e) {
            @Override
            public void run() {
                if (!hasUserData()) return;
                ServerConnectedEvent e = (ServerConnectedEvent) getUserData();
                KeepAlivePacket ka = new KeepAlivePacket(WorldEditGlobalizerBungee.getInstance().getDescription().getVersion());
                Callback callback = new Callback(2000, ka.getIdentifier()) {
                    @Override
                    public void onTimeOut(Callback callback) {
                        if (!hasUserData()) return;
                        ServerConnectedEvent e = (ServerConnectedEvent) getUserData();
                        Player.getPlayer(e.getPlayer()).setPluginOnCurrentServerInstalled(false);
                        KeepAliveRunnable.start(e.getServer(), e.getPlayer());
                    }

                    @Override
                    public void onCallback(Callback callback, Object response) {
                        if (!hasUserData()) return;
                        ServerConnectedEvent e = (ServerConnectedEvent) getUserData();
                        if (response instanceof KeepAlivePacket) {
                            if (!((KeepAlivePacket) response).getVersion().equals(WorldEditGlobalizerBungee.getInstance().getDescription().getVersion())) {
                                Player.getPlayer(e.getPlayer()).setPluginOnCurrentServerInstalled(false);
                                MessageManager.sendMessage(BungeeCord.getInstance().getConsole(), "incompatible.version", WorldEditGlobalizerBungee.getInstance().getDescription().getVersion(), e.getServer().getInfo().getName(), ((KeepAlivePacket) response).getVersion());
                                return;
                            }
                        }
                        Player.getPlayer(e.getPlayer()).setPluginOnCurrentServerInstalled(true);
                        if (Player.getPlayer(e.getPlayer()).hasClipboard()) {
                            if (WorldEditGlobalizerBungee.getInstance().getMainConfig().isEnableClipboardAutoDownload()) {
                                MessageManager.sendMessage(e.getPlayer(), "clipboard.start.downloading");
                                ClipboardSendPacket packet = new ClipboardSendPacket();
                                packet.setClipboardHash(Player.getPlayer(e.getPlayer()).getClipboard().getHash());
                                packet.setData(Player.getPlayer(e.getPlayer()).getClipboard().getData());
                                PacketSender.sendPacket(e.getPlayer(), packet);
                            }
                        }
                        KeepAliveRunnable.start(e.getServer(), e.getPlayer());
                    }
                };
                callback.setUserData(e);
                callback.start();
                PacketSender.sendPacket(e.getPlayer(), ka);

            }
        }, 1000, TimeUnit.MILLISECONDS);

    }

}
