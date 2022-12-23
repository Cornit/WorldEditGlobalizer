package me.illgilp.worldeditglobalizer.common.network.protocol.packet;

import java.io.IOException;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataInput;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataOutput;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract class IdentifiedPacket extends Packet {

    private UUID id;
    private boolean request;

    @Override
    public void read(PacketDataInput in) throws IOException {
        this.id = in.readUUID();
        this.request = in.readBoolean();
        this.readData(in);
    }

    @Override
    public void write(PacketDataOutput out) throws IOException {
        out.writeUUID(this.id);
        out.writeBoolean(this.request);
        this.writeData(out);
    }

    public abstract void readData(PacketDataInput in) throws IOException;

    public abstract void writeData(PacketDataOutput out) throws IOException;
}
