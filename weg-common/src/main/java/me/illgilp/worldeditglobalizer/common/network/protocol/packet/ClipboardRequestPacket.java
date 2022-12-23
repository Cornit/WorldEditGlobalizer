package me.illgilp.worldeditglobalizer.common.network.protocol.packet;

import java.io.IOException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.illgilp.worldeditglobalizer.common.network.AbstractPacketHandler;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataInput;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataOutput;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ClipboardRequestPacket extends Packet {


    @Override
    public void read(PacketDataInput in) throws IOException {
    }

    @Override
    public void write(PacketDataOutput out) throws IOException {
    }

    @Override
    public void handle(AbstractPacketHandler packetHandler) {
        packetHandler.handle(this);
    }
}
