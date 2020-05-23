package me.illgilp.worldeditglobalizerbungee.listener;

import java.util.concurrent.TimeUnit;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.callback.Callback;
import me.illgilp.worldeditglobalizerbungee.callback.PingCallback;
import me.illgilp.worldeditglobalizerbungee.manager.MessageManager;
import me.illgilp.worldeditglobalizerbungee.manager.PlayerManager;
import me.illgilp.worldeditglobalizerbungee.network.PacketSender;
import me.illgilp.worldeditglobalizerbungee.player.ServerUsability;
import me.illgilp.worldeditglobalizerbungee.runnables.KeepAliveRunnable;
import me.illgilp.worldeditglobalizerbungee.runnables.PingRunnable;
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
                PingCallback<ServerConnectedEvent> callback = new PingCallback<ServerConnectedEvent>(2000, PlayerManager.getInstance().getPlayer(e.getPlayer().getUniqueId())) {
                    @Override
                    public void onTimeOut(Callback callback) {
                        if (!hasUserData()) return;
                        if (PlayerManager.getInstance().getPlayer(e.getPlayer().getUniqueId()) == null) return;
                        ServerConnectedEvent e = (ServerConnectedEvent) getUserData();
                        PlayerManager.getInstance().getPlayer(e.getPlayer().getUniqueId()).setServerUsability(ServerUsability.PLUGIN_NOT_INSTALLED);
                        PingRunnable.start(e.getServer(), PlayerManager.getInstance().getPlayer(e.getPlayer().getUniqueId()));
                    }

                    @Override
                    public void onCallback(Callback callback, Boolean response) {
                        if (!hasUserData()) return;
                        if (PlayerManager.getInstance().getPlayer(e.getPlayer().getUniqueId()) == null) return;
                        ServerConnectedEvent e = (ServerConnectedEvent) getUserData();
                        if (!response) {
                            PlayerManager.getInstance().getPlayer(e.getPlayer().getUniqueId()).setServerUsability(ServerUsability.KEY_NOT_SET);
                            PingRunnable.start(e.getServer(), PlayerManager.getInstance().getPlayer(e.getPlayer().getUniqueId()));
                            return;
                        }
                        KeepAlivePacket ka = new KeepAlivePacket(WorldEditGlobalizerBungee.getInstance().getDescription().getVersion());
                        Callback keepAliveCallback = new Callback<ServerConnectedEvent, KeepAlivePacket>(2000, ka.getIdentifier()) {

                            @Override
                            public void onTimeOut(Callback callback) {
                                ServerConnectedEvent e = getUserData();
                                if (PlayerManager.getInstance().getPlayer(e.getPlayer().getUniqueId()) == null) return;
                                PlayerManager.getInstance().getPlayer(e.getPlayer().getUniqueId()).setServerUsability(ServerUsability.KEY_NOT_CORRECT);
                                KeepAliveRunnable.start(e.getServer(), PlayerManager.getInstance().getPlayer(e.getPlayer().getUniqueId()));
                            }

                            @Override
                            public void onCallback(Callback callback, KeepAlivePacket response) {
                                ServerConnectedEvent e = getUserData();
                                if (PlayerManager.getInstance().getPlayer(e.getPlayer().getUniqueId()) != null) {
                                    if (!response.getVersion().equals(WorldEditGlobalizerBungee.getInstance().getDescription().getVersion())) {
                                        PlayerManager.getInstance().getPlayer(e.getPlayer().getUniqueId()).setServerVersion(((KeepAlivePacket) response).getVersion());
                                        PlayerManager.getInstance().getPlayer(e.getPlayer().getUniqueId()).setServerUsability(ServerUsability.INCOMPATIBLE_VERSION);
                                        MessageManager.sendMessage(e.getPlayer(), "incompatible.version", WorldEditGlobalizerBungee.getInstance().getDescription().getVersion(), e.getServer().getInfo().getName(), ((KeepAlivePacket) response).getVersion());
                                        MessageManager.sendMessage(BungeeCord.getInstance().getConsole(), "incompatible.version", WorldEditGlobalizerBungee.getInstance().getDescription().getVersion(), e.getServer().getInfo().getName(), ((KeepAlivePacket) response).getVersion());
                                        return;
                                    }
                                    if (PlayerManager.getInstance().getPlayer(e.getPlayer().getUniqueId()).hasClipboard()) {
                                        if (WorldEditGlobalizerBungee.getInstance().getMainConfig().isEnableClipboardAutoDownload()) {
                                            MessageManager.sendMessage(e.getPlayer(), "clipboard.start.downloading");
                                            ClipboardSendPacket packet = new ClipboardSendPacket();
                                            packet.setClipboardHash(PlayerManager.getInstance().getPlayer(e.getPlayer().getUniqueId()).getClipboard().getHash());
                                            packet.setData(PlayerManager.getInstance().getPlayer(e.getPlayer().getUniqueId()).getClipboard().getData());
                                            PacketSender.sendPacket(e.getPlayer(), packet);
                                        }
                                    }
                                    KeepAliveRunnable.start(e.getServer(), PlayerManager.getInstance().getPlayer(e.getPlayer().getUniqueId()));
                                }
                            }
                        };
                        PacketSender.sendPacket(e.getPlayer(), ka);
                        keepAliveCallback.setUserData(e);
                        keepAliveCallback.start();
                    }
                };
                callback.setUserData(e);
                callback.start();

            }
        }, 2500, TimeUnit.MILLISECONDS);

    }

}
