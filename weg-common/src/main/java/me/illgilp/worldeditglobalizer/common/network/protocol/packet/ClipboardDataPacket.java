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
public class ClipboardDataPacket extends Packet {

    private int hash;
    private byte[] clipboardData;

    @Override
    public void read(PacketDataInput in) throws IOException {
        this.hash = in.readInt();
        this.clipboardData = in.readSizedBytes();
    }

    @Override
    public void write(PacketDataOutput out) throws IOException {
        out.writeInt(this.hash);
        out.writeSizedBytes(this.clipboardData);
    }

    @Override
    public boolean canCancelPacketSending(Packet other) {
        return other instanceof ClipboardDataPacket;
    }

    @Override
    public boolean canBeSentImmediately() {
        return false;
    }

    @Override
    public void handle(AbstractPacketHandler packetHandler) {
        packetHandler.handle(this);
    }
}
