package me.illgilp.worldeditglobalizerbungee.listener;

import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.Map;

public class PluginMessageListener implements Listener {

    private Map<String, PacketDataSerializer> unFinishedPackets = new HashMap<>();


    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        if (!(e.getReceiver() instanceof ProxiedPlayer)) return;
        String channel = e.getTag();
        byte[] bytes = e.getData();
        ProxiedPlayer player = (ProxiedPlayer) e.getReceiver();
        if (channel.equals("worldeditglobalizer:connection")) {
            PacketDataSerializer data = new PacketDataSerializer(bytes);
            int packetid = data.readVarInt();
            boolean splitted = data.readBoolean();
            if (splitted) {
                int splitCount = data.readInt();
                int currentSplit = data.readInt();


                PacketDataSerializer serializer = new PacketDataSerializer();

                if (unFinishedPackets.containsKey(player.getName() + packetid + splitted + splitCount)) {
                    serializer = unFinishedPackets.get(player.getName() + packetid + splitted + splitCount);
                }
                serializer.writeFinalArray(data.readArray());
                unFinishedPackets.put(player.getName() + packetid + splitted + splitCount, serializer);

                if (currentSplit == (splitCount - 1)) {
                    WorldEditGlobalizerBungee.getInstance().getPacketManager().callPacket(player, packetid, unFinishedPackets.get(player.getName() + packetid + splitted + splitCount).toByteArray(), (ServerConnection) e.getSender());
                    unFinishedPackets.remove(player.getName() + packetid + splitted + splitCount);
                }

            } else {
                WorldEditGlobalizerBungee.getInstance().getPacketManager().callPacket(player, packetid, data.readArray(), (ServerConnection) e.getSender());
            }
            e.setCancelled(true);
        }
    }

}
