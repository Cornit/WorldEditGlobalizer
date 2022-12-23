package me.illgilp.worldeditglobalizer.common.network.protocol.packet;

import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.illgilp.worldeditglobalizer.common.network.AbstractPacketHandler;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataInput;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataOutput;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ActionBarPacket extends Packet {

    private Component message;

    @Override
    public void read(PacketDataInput in) throws IOException {
        message = GsonComponentSerializer.gson().deserialize(in.readString());
    }

    @Override
    public void write(PacketDataOutput out) throws IOException {
        out.writeString(GsonComponentSerializer.gson().serialize(message));
    }

    @Override
    public void handle(AbstractPacketHandler packetHandler) {
        packetHandler.handle(this);
    }

}
