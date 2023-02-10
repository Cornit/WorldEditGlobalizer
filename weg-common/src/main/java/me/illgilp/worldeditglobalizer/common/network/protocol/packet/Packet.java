package me.illgilp.worldeditglobalizer.common.network.protocol.packet;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import me.illgilp.worldeditglobalizer.common.network.AbstractPacketHandler;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataInput;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataOutput;

@RequiredArgsConstructor
public abstract class Packet {

    public abstract void read(PacketDataInput in) throws IOException;

    public abstract void write(PacketDataOutput out) throws IOException;

    public abstract void handle(AbstractPacketHandler packetHandler);

    public boolean canCancelPacketSending(Packet other) {
        return false;
    }

    public boolean canBeSentImmediately() {
        return true;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();
}
