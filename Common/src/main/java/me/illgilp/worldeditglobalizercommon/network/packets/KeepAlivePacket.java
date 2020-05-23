package me.illgilp.worldeditglobalizercommon.network.packets;

import java.util.Objects;
import java.util.UUID;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;

public class KeepAlivePacket extends Packet {

    private UUID identifier = UUID.randomUUID();
    private String version;

    public KeepAlivePacket() {
    }

    public KeepAlivePacket(UUID identifier, String version) {
        this.identifier = identifier;
        this.version = version;
    }

    public KeepAlivePacket(String version) {
        this.version = version;
    }

    @Override
    public void read(PacketDataSerializer buf) {
        identifier = UUID.fromString(buf.readString());
        version = buf.readString();
    }

    @Override
    public void write(PacketDataSerializer buf) {
        buf.writeString(identifier.toString());
        buf.writeString(version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeepAlivePacket that = (KeepAlivePacket) o;
        return Objects.equals(identifier, that.identifier) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, version);
    }

    @Override
    public String toString() {
        return "KeepAlivePacket{" +
                "identifier=" + identifier +
                ", version='" + version + '\'' +
                '}';
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public void setIdentifier(UUID identifier) {
        this.identifier = identifier;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
