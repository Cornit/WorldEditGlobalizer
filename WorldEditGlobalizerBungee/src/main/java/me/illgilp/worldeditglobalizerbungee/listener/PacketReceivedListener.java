package me.illgilp.worldeditglobalizerbungee.listener;

import me.illgilp.worldeditglobalizerbungee.Callback;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.clipboard.Clipboard;
import me.illgilp.worldeditglobalizerbungee.events.PacketReceivedEvent;
import me.illgilp.worldeditglobalizerbungee.manager.MessageManager;
import me.illgilp.worldeditglobalizerbungee.network.PacketSender;
import me.illgilp.worldeditglobalizerbungee.player.Player;
import me.illgilp.worldeditglobalizerbungee.util.StringUtils;
import me.illgilp.worldeditglobalizercommon.network.packets.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.Map;

public class PacketReceivedListener implements Listener {

    @EventHandler
    public void onPacketReceived(PacketReceivedEvent e) {
        if (e.getPacket() instanceof ClipboardSendPacket) {
            if (Callback.callback(((ClipboardSendPacket) e.getPacket()).getIdentifier(), e.getPacket()) != null) {
                return;
            }
            Clipboard clipboard = new Clipboard(e.getPlayer().getUniqueId(), ((ClipboardSendPacket) e.getPacket()).getData(), ((ClipboardSendPacket) e.getPacket()).getClipboardhash(), e.getServer().getInfo().getName());
            Player.getPlayer(e.getPlayer()).setClipboard(clipboard);
            MessageManager.sendMessage(e.getPlayer(), "clipboard.finish.uploading", StringUtils.humanReadableByteCount(clipboard.getData().length, true));
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
            PacketSender.sendPacket(player, resp);
        } else if (e.getPacket() instanceof MessageRequestPacket) {
            MessageRequestPacket packet = (MessageRequestPacket) e.getPacket();
            MessageResponsePacket resp = new MessageResponsePacket();
            resp.setIdentifier(packet.getIdentifier());
            resp.setPath(packet.getPath());
            if (packet.getLanguage().equalsIgnoreCase("default")) {
                resp.setMessage(MessageManager.getMessage(MessageManager.getInstance().getLanguage(), packet.getPath(), packet.getPlaceholders()));
                resp.setLanguage(MessageManager.getInstance().getLanguage());
            } else {
                resp.setMessage(MessageManager.getMessage(packet.getLanguage(), packet.getPath(), packet.getPlaceholders()));
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
            packet.setClipboardhash(Player.getPlayer(e.getPlayer()).getClipboard().getHash());
            packet.setData(Player.getPlayer(e.getPlayer()).getClipboard().getData());
            PacketSender.sendPacket(e.getPlayer(), packet);
        } else if (e.getPacket() instanceof PluginConfigRequestPacket) {
            PluginConfigRequestPacket req = (PluginConfigRequestPacket) e.getPacket();
            PluginConfigResponsePacket res = new PluginConfigResponsePacket();
            res.setIdentifier(req.getIdentifier());
            res.setLanguage(WorldEditGlobalizerBungee.getInstance().getMainConfig().getLanguage());
            res.setMaxClipboardSize(WorldEditGlobalizerBungee.getInstance().getMainConfig().getMaxClipboardBytes());
            res.setKeepClipboard(WorldEditGlobalizerBungee.getInstance().getMainConfig().isKeepClipboard());
            res.setPrefix(WorldEditGlobalizerBungee.getInstance().getMainConfig().getPrefix());
            PacketSender.sendPacket(e.getPlayer(), res);
        } else if (e.getPacket() instanceof KeepAlivePacket) {
            Callback.callback(((KeepAlivePacket) e.getPacket()).getIdentifier(), e.getPacket());
        }
    }

}
