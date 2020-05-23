package me.illgilp.worldeditglobalizercommon.network.packets;

import java.util.UUID;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;

public class PluginConfigRequestPacket extends Packet {

    private UUID identifier = UUID.randomUUID();

    @Override
    public void read(PacketDataSerializer buf) {
        identifier = UUID.fromString(buf.readString());

    }

    @Override
    public void write(PacketDataSerializer buf) {
        buf.writeString(identifier.toString());

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PluginConfigRequestPacket)) return false;

        PluginConfigRequestPacket that = (PluginConfigRequestPacket) o;

        return getIdentifier().equals(that.getIdentifier());
    }

    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
    }

    @Override
    public String toString() {
        return "PluginConfigRequestPacket{" +
                "identifier=" + identifier +
                '}';
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public void setIdentifier(UUID identifier) {
        this.identifier = identifier;
    }

}
