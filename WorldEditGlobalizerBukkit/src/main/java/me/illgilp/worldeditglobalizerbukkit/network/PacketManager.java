package me.illgilp.worldeditglobalizerbukkit.network;


import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import me.illgilp.worldeditglobalizerbukkit.WorldEditGlobalizerBukkit;
import me.illgilp.worldeditglobalizerbukkit.events.PacketReceivedEvent;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;
import me.illgilp.worldeditglobalizercommon.network.packets.Packet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PacketManager {

    private final Map<Packet.Direction, Map<Integer, Class<? extends Packet>>> registeredPackets = new HashMap<>();


    public void registerPacket(Packet.Direction direction, Class<? extends Packet> clazz, int packetId) {
        int packetid = -1;
        packetid = packetId;

        if (packetid == -1) {
            throw new IllegalArgumentException("PacketId for PacketClass: " + clazz.getName() + " wasn't set!");
        }
        Map<Integer, Class<? extends Packet>> packets = registeredPackets.get(direction);
        if (packets == null) packets = new HashMap<>();
        packets.put(packetid, clazz);
        registeredPackets.put(direction, packets);

    }

    public Map<Packet.Direction, Map<Integer, Class<? extends Packet>>> getRegisteredPackets() {
        return registeredPackets;
    }

    public int getPacketId(Packet.Direction direction, Class<? extends Packet> clazz) {
        for (int id : registeredPackets
                .get(direction)
                .keySet()) {
            if (registeredPackets.get(direction).get(id).equals(clazz)) {
                return id;
            }
        }
        return -1;
    }

    public void callPacket(Player player, int packetId, byte[] data) {
        try {
            if (registeredPackets
                    .get(Packet.Direction.TO_BUKKIT)
                    .containsKey(packetId)) {
                Packet packet = null;
                try {
                    packet = (Packet) registeredPackets.get(Packet.Direction.TO_BUKKIT).get(packetId).newInstance();
                    packet.read(new PacketDataSerializer(data));
                } catch (Exception e) {
                    WorldEditGlobalizerBukkit.getInstance().getLogger().log(Level.SEVERE, "Error while reading packet: " + packet.getClass().getSimpleName(), e);
                }
                PacketReceivedEvent event = new PacketReceivedEvent(player, packet, packetId);
                Bukkit.getPluginManager().callEvent(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }


}
