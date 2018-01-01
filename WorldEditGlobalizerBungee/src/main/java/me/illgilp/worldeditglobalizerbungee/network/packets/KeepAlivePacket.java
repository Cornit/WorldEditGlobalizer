package me.illgilp.worldeditglobalizerbungee.network.packets;

import me.illgilp.worldeditglobalizerbungee.util.PacketDataSerializer;

import java.util.Random;
import java.util.UUID;

public class KeepAlivePacket extends Packet {

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
        if (!(o instanceof KeepAlivePacket)) return false;

        KeepAlivePacket that = (KeepAlivePacket) o;

        return identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public String toString() {
        return "KeepAlivePacket{" +
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
