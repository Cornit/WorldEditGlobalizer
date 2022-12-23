package me.illgilp.worldeditglobalizer.common.network.protocol.packet;

import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.illgilp.worldeditglobalizer.common.network.AbstractPacketHandler;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataInput;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataOutput;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AutoUploadReadyPacket extends Packet {

    private boolean ready;

    @Override
    public void read(PacketDataInput in) throws IOException {
        this.ready = in.readBoolean();
    }

    @Override
    public void write(PacketDataOutput out) throws IOException {
        out.writeBoolean(this.ready);
    }

    @Override
    public void handle(AbstractPacketHandler packetHandler) {
        packetHandler.handle(this);
    }

}
