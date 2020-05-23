package me.illgilp.worldeditglobalizercommon.network.packets;


import java.util.Arrays;
import java.util.UUID;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;

public class PluginSendPacket extends Packet {

    private UUID identifier = UUID.randomUUID();
    private int tryNum;
    private byte[] hash;
    private byte[] data;

    @Override
    public void read(PacketDataSerializer buf) {
        identifier = UUID.fromString(buf.readString());
        hash = buf.readByteArray(32);
        data = buf.readArray();
    }

    @Override
    public void write(PacketDataSerializer buf) {
        buf.writeString(identifier.toString());
        buf.writeByteArray(hash);
        buf.writeArray(data);
    }


    public PluginSendPacket() {
    }

    public PluginSendPacket(UUID identifier, byte[] hash, byte[] data) {
        this.identifier = identifier;
        this.hash = hash;
        this.data = data;
    }

    public PluginSendPacket(UUID identifier, int tryNum, byte[] hash, byte[] data) {
        this.identifier = identifier;
        this.tryNum = tryNum;
        this.hash = hash;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PluginSendPacket that = (PluginSendPacket) o;

        if (tryNum != that.tryNum) {
            return false;
        }
        if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) {
            return false;
        }
        if (!Arrays.equals(hash, that.hash)) {
            return false;
        }
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + tryNum;
        result = 31 * result + Arrays.hashCode(hash);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public String toString() {
        return "PluginSendPacket{" +
            "identifier=" + identifier +
            ", tryNum=" + tryNum +
            '}';
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public void setIdentifier(UUID identifier) {
        this.identifier = identifier;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getTryNum() {
        return tryNum;
    }

    public void setTryNum(int tryNum) {
        this.tryNum = tryNum;
    }
}
