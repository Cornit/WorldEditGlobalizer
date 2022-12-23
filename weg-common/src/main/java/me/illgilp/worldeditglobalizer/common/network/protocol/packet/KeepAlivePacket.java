package me.illgilp.worldeditglobalizer.common.network.protocol.packet;

import java.io.IOException;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.illgilp.worldeditglobalizer.common.network.AbstractPacketHandler;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataInput;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataOutput;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class KeepAlivePacket extends IdentifiedPacket {

    public KeepAlivePacket(UUID id, boolean request) {
        super(id, request);
    }

    @Override
    public void readData(PacketDataInput in) throws IOException {

    }

    @Override
    public void writeData(PacketDataOutput out) throws IOException {

    }

    @Override
    public void handle(AbstractPacketHandler packetHandler) {
        packetHandler.handle(this);
    }
}
