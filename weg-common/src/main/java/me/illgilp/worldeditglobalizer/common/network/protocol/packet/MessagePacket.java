package me.illgilp.worldeditglobalizer.common.network.protocol.packet;

import java.io.IOException;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.illgilp.worldeditglobalizer.common.network.AbstractPacketHandler;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataInput;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataOutput;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MessagePacket extends Packet {

    private UUID source;
    private Component message;
    private MessageType messageType;

    @Override
    public void read(PacketDataInput in) throws IOException {
        source = in.readUUID();
        message = GsonComponentSerializer.gson().deserialize(in.readString());
        messageType = MessageType.valueOf(in.readString());
    }

    @Override
    public void write(PacketDataOutput out) throws IOException {
        out.writeUUID(this.source);
        out.writeString(GsonComponentSerializer.gson().serialize(message));
        out.writeString(messageType.name());
    }

    @Override
    public void handle(AbstractPacketHandler packetHandler) {
        packetHandler.handle(this);
    }

}
