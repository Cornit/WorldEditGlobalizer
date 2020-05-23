package me.illgilp.worldeditglobalizerbungee.listener;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.callback.Callback;
import me.illgilp.worldeditglobalizerbungee.manager.MessageManager;
import me.illgilp.worldeditglobalizerbungee.manager.PlayerManager;
import me.illgilp.worldeditglobalizerbungee.player.ServerUsability;
import me.illgilp.worldeditglobalizerbungee.util.StringUtil;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;
import me.illgilp.worldeditglobalizercommon.util.Signature;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PluginMessageListener implements Listener {

    private Map<String, PacketDataSerializer> unFinishedPackets = new HashMap<>();


    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        if (!(e.getReceiver() instanceof ProxiedPlayer)) return;
        String channel = e.getTag();
        byte[] bytes = e.getData();
        ProxiedPlayer player = (ProxiedPlayer) e.getReceiver();
        if (channel.equals("weg:ping")) {
            PacketDataSerializer packetDataSerializer = new PacketDataSerializer(bytes);
            int type = packetDataSerializer.readByte();
            if (type == 1) {
                if (bytes.length == (1 + 8 + 8 + 1)) {
                    UUID id = packetDataSerializer.readUUID();
                    boolean keySet = packetDataSerializer.readBoolean();
                    Callback.callback(id, keySet);
                }
            } else if (type == 2) {
                if (bytes.length == 1) {
                    if (PlayerManager.getInstance().getPlayer(player.getUniqueId()) != null) {
                        PlayerManager.getInstance().getPlayer(player.getUniqueId()).setServerUsability(ServerUsability.KEY_NOT_CORRECT);
                    }
                }
            }
            return;
        }
        if (channel.equals("weg:connection")) {
            PacketDataSerializer sig = new PacketDataSerializer(bytes);
            byte[] sign = sig.readByteArray(32);
            byte[] dataB = sig.readByteArray(bytes.length - 32);
            Signature signature = new Signature();
            signature.setData(dataB);
            signature.setKey(WorldEditGlobalizerBungee.getInstance().getMainConfig().getSecretKey().getBytes(StandardCharsets.UTF_8));
            if(!signature.verify(sign)) return;

            PacketDataSerializer data = new PacketDataSerializer(dataB);

            int packetid = data.readVarInt();
            boolean splitted = data.readBoolean();
            if (splitted) {
                long packetSize = data.readVarLong();
                int splitCount = data.readInt();
                int currentSplit = data.readInt();


                PacketDataSerializer serializer = new PacketDataSerializer();

                if (unFinishedPackets.containsKey(player.getName() + packetid + splitted + splitCount)) {
                    serializer = unFinishedPackets.get(player.getName() + packetid + splitted + splitCount);
                }
                serializer.writeByteArray(data.readArray());
                unFinishedPackets.put(player.getName() + packetid + splitted + splitCount, serializer);

                if (currentSplit == (splitCount - 1)) {
                    WorldEditGlobalizerBungee.getInstance().getPacketManager().callPacket(player, packetid, unFinishedPackets.get(player.getName() + packetid + splitted + splitCount).toByteArray(), (ServerConnection) e.getSender());
                    unFinishedPackets.remove(player.getName() + packetid + splitted + splitCount);
                }

                int perc = (int) Math.round((double)serializer.toByteArray().length / (double) packetSize * 100.0);
                double decPerc = (double)serializer.toByteArray().length / (double) packetSize;
                String msg = MessageManager.getRawMessageOrEmpty("actionbar.progress.upload", StringUtil.intToLengthedString(perc, 3));
                msg = ChatColor.stripColor(msg);
                int po = (int) Math.round((double) msg.length() * decPerc);
                String fin = "§a";
                if (po < msg.length()) {
                    fin += msg.substring(0, po);
                    fin += "§r";
                    fin += msg.substring(po);
                } else {
                    fin += msg;
                }

                player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(fin));

            } else {
                WorldEditGlobalizerBungee.getInstance().getPacketManager().callPacket(player, packetid, data.readArray(), (ServerConnection) e.getSender());
            }
            e.setCancelled(true);
        }
    }

}
