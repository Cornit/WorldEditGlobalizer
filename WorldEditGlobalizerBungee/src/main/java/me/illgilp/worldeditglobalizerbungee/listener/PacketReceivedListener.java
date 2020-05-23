package me.illgilp.worldeditglobalizerbungee.listener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.callback.Callback;
import me.illgilp.worldeditglobalizerbungee.clipboard.Clipboard;
import me.illgilp.worldeditglobalizerbungee.events.PacketReceivedEvent;
import me.illgilp.worldeditglobalizerbungee.manager.MessageManager;
import me.illgilp.worldeditglobalizerbungee.network.PacketSender;
import me.illgilp.worldeditglobalizerbungee.util.StringUtil;
import me.illgilp.worldeditglobalizercommon.network.packets.ClipboardRequestPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.ClipboardSendPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.KeepAlivePacket;
import me.illgilp.worldeditglobalizercommon.network.packets.MessageRequestPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.MessageResponsePacket;
import me.illgilp.worldeditglobalizercommon.network.packets.PermissionCheckRequestPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.PermissionCheckResponsePacket;
import me.illgilp.worldeditglobalizercommon.network.packets.PluginConfigRequestPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.PluginConfigResponsePacket;
import me.illgilp.worldeditglobalizercommon.network.packets.PluginSendPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.PluginSendResultPacket;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.apache.commons.io.IOUtils;

public class PacketReceivedListener implements Listener {

    @EventHandler
    public void onPacketReceived(PacketReceivedEvent e) {
        if (e.getPacket() instanceof ClipboardSendPacket) {
            if (Callback.callback(((ClipboardSendPacket) e.getPacket()).getIdentifier(), e.getPacket()) != null) {
                return;
            }
            Clipboard clipboard = new Clipboard(e.getPlayer().getUniqueId(), ((ClipboardSendPacket) e.getPacket()).getData(), ((ClipboardSendPacket) e.getPacket()).getClipboardHash(), e.getServer().getInfo().getName());
            e.getPlayer().setClipboard(clipboard);
            MessageManager.sendMessage(e.getPlayer(), "clipboard.finish.uploading", StringUtil.humanReadableByteCount(clipboard.getData().length, true));
        } else if (e.getPacket() instanceof PermissionCheckRequestPacket) {
            PermissionCheckRequestPacket packet = (PermissionCheckRequestPacket) e.getPacket();
            Map<String, Boolean> map = new HashMap<>();
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(packet.getPlayer());
            if (player == null) return;
            for (String permission : packet.getPermissions()) {
                map.put(permission, ((player != null) && player.hasPermission(permission)));
            }
            PermissionCheckResponsePacket resp = new PermissionCheckResponsePacket();
            resp.setIdentifier(packet.getIdentifier());
            resp.setPlayer(packet.getPlayer());
            resp.setPermissions(map);
            PacketSender.sendPacket(e.getPlayer(), resp);
        } else if (e.getPacket() instanceof MessageRequestPacket) {
            MessageRequestPacket packet = (MessageRequestPacket) e.getPacket();
            MessageResponsePacket resp = new MessageResponsePacket();
            resp.setIdentifier(packet.getIdentifier());
            resp.setPath(packet.getPath());
            if (packet.getLanguage().equalsIgnoreCase("default")) {
                resp.setJson(MessageManager.toJson(MessageManager.getMessage(MessageManager.getInstance().getLanguage(), packet.getPath(), packet.getPlaceholders())));
                resp.setLanguage(MessageManager.getInstance().getLanguage());
            } else {
                resp.setJson(MessageManager.toJson(MessageManager.getMessage(packet.getLanguage(), packet.getPath(), packet.getPlaceholders())));
                if (MessageManager.getInstance().hasMessageFile(packet.getLanguage())) {
                    resp.setLanguage(packet.getLanguage());
                } else {
                    resp.setLanguage(MessageManager.getInstance().getLanguage());
                }
            }
            PacketSender.sendPacket(e.getPlayer(), resp);
        } else if (e.getPacket() instanceof ClipboardRequestPacket) {
            ClipboardRequestPacket req = (ClipboardRequestPacket) e.getPacket();
            ClipboardSendPacket packet = new ClipboardSendPacket();
            packet.setIdentifier(req.getIdentifier());
            packet.setClipboardHash(e.getPlayer().getClipboard().getHash());
            packet.setData(e.getPlayer().getClipboard().getData());
            PacketSender.sendPacket(e.getPlayer(), packet);
        } else if (e.getPacket() instanceof PluginConfigRequestPacket) {
            PluginConfigRequestPacket req = (PluginConfigRequestPacket) e.getPacket();
            PluginConfigResponsePacket res = new PluginConfigResponsePacket();
            res.setIdentifier(req.getIdentifier());
            res.setLanguage(WorldEditGlobalizerBungee.getInstance().getMainConfig().getLanguage());
            res.setMaxClipboardSize(WorldEditGlobalizerBungee.getInstance().getMainConfig().getMaxClipboardBytes());
            res.setKeepClipboard(WorldEditGlobalizerBungee.getInstance().getMainConfig().isKeepClipboard());
            res.setPrefix(WorldEditGlobalizerBungee.getInstance().getMainConfig().getPrefix());
            res.setEnableClipboardAutoDownload(WorldEditGlobalizerBungee.getInstance().getMainConfig().isEnableClipboardAutoDownload());
            res.setEnableClipboardAutoUpload(WorldEditGlobalizerBungee.getInstance().getMainConfig().isEnableClipboardAutoUpload());
            PacketSender.sendPacket(e.getPlayer(), res);
        } else if (e.getPacket() instanceof KeepAlivePacket) {
            Callback.callback(((KeepAlivePacket) e.getPacket()).getIdentifier(), e.getPacket());
        } else if (e.getPacket() instanceof PluginSendResultPacket) {
            if (((PluginSendResultPacket) e.getPacket()).getResult() == PluginSendResultPacket.Result.FAILED) {
                if (((PluginSendResultPacket) e.getPacket()).getTryNum() < 3) {
                    MessageManager.sendMessage(e.getPlayer(), "command.error.syncversions.tryAgain");
                    try {
                        PluginSendPacket pluginSendPacket = new PluginSendPacket();
                        FileInputStream fileInputStream = new FileInputStream(WorldEditGlobalizerBungee.getInstance().getFile());
                        byte[] data = IOUtils.toByteArray(fileInputStream);
                        fileInputStream.close();
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        byte[] hash = digest.digest(data);
                        pluginSendPacket.setData(data);
                        pluginSendPacket.setHash(hash);
                        pluginSendPacket.setTryNum(((PluginSendResultPacket) e.getPacket()).getTryNum() + 1);
                        PacketSender.sendPacket(e.getPlayer(), pluginSendPacket);
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (NoSuchAlgorithmException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    MessageManager.sendMessage(e.getPlayer(), "command.error.syncversions.failed");
                }
            } else if (((PluginSendResultPacket) e.getPacket()).getResult() == PluginSendResultPacket.Result.SUCCESS) {
                MessageManager.sendMessage(e.getPlayer(), "command.finish.syncversions");
            }
        }
    }

}
