package me.illgilp.worldeditglobalizerbukkit.listener;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.illgilp.worldeditglobalizerbukkit.WorldEditGlobalizerBukkit;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;
import me.illgilp.worldeditglobalizercommon.util.Signature;
import org.bukkit.entity.Player;

public class PluginMessageListener implements org.bukkit.plugin.messaging.PluginMessageListener {

    private Map<String, PacketDataSerializer> unFinishedPackets = new HashMap<>();

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (channel.equals("weg:ping")) {
            PacketDataSerializer read = new PacketDataSerializer(bytes);
            PacketDataSerializer packetDataSerializer = new PacketDataSerializer();
            packetDataSerializer.writeByte((byte) 1);
            UUID uuid = read.readUUID();
            packetDataSerializer.writeUUID(uuid);
            packetDataSerializer.writeBoolean(!WorldEditGlobalizerBukkit.getInstance().getMainConfig().getSecretKey().equals("PUT KEY IN HERE"));
            player.sendPluginMessage(WorldEditGlobalizerBukkit.getInstance(), "weg:ping", packetDataSerializer.toByteArray());
            return;
        }
        if (channel.equals("weg:connection")) {
            PacketDataSerializer sig = new PacketDataSerializer(bytes);
            byte[] sign = sig.readByteArray(32);
            byte[] dataB = sig.readByteArray(bytes.length - 32);
            Signature signature = new Signature();
            signature.setData(dataB);
            String key = WorldEditGlobalizerBukkit.getInstance().getMainConfig().getSecretKey();
            boolean keySet = true;
            if (key.equals("PUT KEY IN HERE")) {
                key = UUID.randomUUID().toString().replace("-","");
                keySet = false;
            }
            signature.setKey(key.getBytes(StandardCharsets.UTF_8));
            if(!signature.verify(sign)) {
                if (keySet) {
                    PacketDataSerializer packetDataSerializer = new PacketDataSerializer();
                    packetDataSerializer.writeByte((byte) 2);
                    player.sendPluginMessage(WorldEditGlobalizerBukkit.getInstance(), "weg:ping", packetDataSerializer.toByteArray());
                }
                return;
            };

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
                    WorldEditGlobalizerBukkit.getInstance().getPacketManager().callPacket(player, packetid, unFinishedPackets.get(player.getName() + packetid + splitted + splitCount).toByteArray());
                    unFinishedPackets.remove(player.getName() + packetid + splitted + splitCount);
                }

            } else {
                WorldEditGlobalizerBukkit.getInstance().getPacketManager().callPacket(player, packetid, data.readArray());
            }
        }
    }
}
