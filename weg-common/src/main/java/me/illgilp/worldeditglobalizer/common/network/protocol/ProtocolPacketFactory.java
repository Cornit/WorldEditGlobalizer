package me.illgilp.worldeditglobalizer.common.network.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.Packet;

@AllArgsConstructor
@Getter
public class ProtocolPacketFactory implements PacketFactory {

    private final Protocol protocol;

    @Override
    public Packet createPacket(int id) {
        return this.protocol.createPacket(id);
    }

    @Override
    public int getPacketId(Class<? extends Packet> packet) {
        return this.protocol.getId(packet);
    }
}
