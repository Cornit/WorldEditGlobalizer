package me.illgilp.worldeditglobalizerbungee.network;


import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.events.PacketReceivedEvent;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;
import me.illgilp.worldeditglobalizercommon.network.packets.Packet;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class PacketManager {

    private Map<Packet.Direction, Map<Integer, Class>> registeredPackets = new HashMap<>();


    public void registerPacket(Packet.Direction direction, Class clazz, int packetId) {
        int packetid = -1;
        packetid = packetId;

        if (packetid == -1) {
            throw new IllegalArgumentException("PacketId for PacketClass: " + clazz.getName() + " wasn't set!");
        }
        Map<Integer, Class> packets = registeredPackets.get(direction);
        if (packets == null) packets = new HashMap<>();
        packets.put(packetid, clazz);
        registeredPackets.put(direction, packets);

    }

    public Map<Packet.Direction, Map<Integer, Class>> getRegisteredPackets() {
        return registeredPackets;
    }

    public int getPacketId(Packet.Direction direction, Class clazz) {
        for (int id : registeredPackets
                .get(direction)
                .keySet()) {
            if (registeredPackets.get(direction).get(id).equals(clazz)) {
                return id;
            }
        }
        return -1;
    }

    public void callPacket(ProxiedPlayer player, int packetId, byte[] data, ServerConnection server) {
        //System.out.println(0);
        try {
            if (registeredPackets
                    .get(Packet.Direction.TO_BUNGEE)
                    .containsKey(packetId)) {
                //System.out.println(1);
                Packet packet = null;
                try {
                    //System.out.println(2);
                    packet = (Packet) registeredPackets.get(Packet.Direction.TO_BUNGEE).get(packetId).newInstance();
                    //System.out.println(3);
                    packet.read(new PacketDataSerializer(data));
                } catch (Exception e) {
                    WorldEditGlobalizerBungee.getInstance().getLogger().log(Level.SEVERE, "Error while reading packet: " + packet.getClass().getSimpleName(), e);
                }
                PacketReceivedEvent event = new PacketReceivedEvent(player, packet, packetId, server);
                WorldEditGlobalizerBungee.getInstance().getProxy().getPluginManager().callEvent(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }


}
