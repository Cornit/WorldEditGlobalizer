package me.illgilp.worldeditglobalizerbukkit.listener;

import me.illgilp.worldeditglobalizerbukkit.WorldEditGlobalizerBukkit;
import me.illgilp.worldeditglobalizerbukkit.util.PacketDataSerializer;
import org.bukkit.craftbukkit.v1_12_R1.generator.SkyLandsChunkGenerator;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PluginMessageListener implements org.bukkit.plugin.messaging.PluginMessageListener {

    private Map<String,PacketDataSerializer> unFinishedPackets = new HashMap<>();

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (channel.equals("WorldEditGlobalizer")) {
            PacketDataSerializer data = new PacketDataSerializer(bytes);
            int packetid = data.readVarInt();
            boolean splitted = data.readBoolean();
            if (splitted) {
                int splitCount = data.readInt();
                int currentSplit = data.readInt();


                PacketDataSerializer serializer = new PacketDataSerializer();

                if(unFinishedPackets.containsKey(player.getName()+packetid+splitted+splitCount)){
                    serializer = unFinishedPackets.get(player.getName()+packetid+splitted+splitCount);
                }
                serializer.writeFinalArray(data.readArray());
                unFinishedPackets.put(player.getName()+packetid+splitted+splitCount,serializer);

                if(currentSplit == (splitCount-1)){
                    WorldEditGlobalizerBukkit.getInstance().getPacketManager().callPacket(player,packetid,unFinishedPackets.get(player.getName()+packetid+splitted+splitCount).toByteArray());
                    unFinishedPackets.remove(player.getName()+packetid+splitted+splitCount);
                }

            }else {
                WorldEditGlobalizerBukkit.getInstance().getPacketManager().callPacket(player,packetid,data.readArray());
            }
        }
    }
}
