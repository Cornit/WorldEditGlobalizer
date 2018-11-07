package me.illgilp.worldeditglobalizersponge.listener;

import me.illgilp.worldeditglobalizersponge.WorldEditGlobalizerSponge;
import me.illgilp.worldeditglobalizersponge.util.PacketDataSerializer;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class PluginMessageListener implements RawDataListener {

    private Map<String,PacketDataSerializer> unFinishedPackets = new HashMap<>();

    @Override
    public void handlePayload(ChannelBuf dataBuf, RemoteConnection connection, Platform.Type side) {
        Player player = ((PlayerConnection)connection).getPlayer();
        PacketDataSerializer data = new PacketDataSerializer(dataBuf.readBytes(dataBuf.available()));
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
                WorldEditGlobalizerSponge.getInstance().getPacketManager().callPacket(player,packetid,unFinishedPackets.get(player.getName()+packetid+splitted+splitCount).toByteArray());
                unFinishedPackets.remove(player.getName()+packetid+splitted+splitCount);
            }
        }else {
            WorldEditGlobalizerSponge.getInstance().getPacketManager().callPacket(player,packetid,data.readArray());
        }

    }
}
