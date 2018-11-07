package me.illgilp.worldeditglobalizersponge.network.packets;

import me.illgilp.worldeditglobalizersponge.util.PacketDataSerializer;

import java.util.UUID;

public class ClipboardRequestPacket extends Packet {

    private UUID identifier = UUID.randomUUID();



    @Override
    public void read(PacketDataSerializer buf) {
        identifier = UUID.fromString(buf.readString());
    }

    @Override
    public void write(PacketDataSerializer buf) {
        buf.writeString(identifier.toString());
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public void setIdentifier(UUID identifier) {
        this.identifier = identifier;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClipboardRequestPacket)) return false;

        ClipboardRequestPacket that = (ClipboardRequestPacket) o;

        return getIdentifier().equals(that.getIdentifier());
    }

    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
    }

    @Override
    public String toString() {
        return "ClipboardRequestPacket{" +
                "identifier=" + identifier +
                '}';
    }
}
