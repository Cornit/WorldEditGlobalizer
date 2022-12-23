package me.illgilp.worldeditglobalizer.common.network.protocol;

import me.illgilp.worldeditglobalizer.common.network.protocol.packet.Packet;

public interface PacketFactory {

    Packet createPacket(int id);

    int getPacketId(Class<? extends Packet> packet);

}
