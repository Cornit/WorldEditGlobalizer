package me.illgilp.worldeditglobalizercommon.network.packets;


import java.util.Arrays;
import java.util.UUID;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;

public class ClipboardSendPacket extends Packet {

    private UUID identifier = UUID.randomUUID();
    private int clipboardHash;
    private byte[] data;

    @Override
    public void read(PacketDataSerializer buf) {
        identifier = UUID.fromString(buf.readString());
        clipboardHash = buf.readInt();
        data = buf.readArray();
    }

    @Override
    public void write(PacketDataSerializer buf) {
        buf.writeString(identifier.toString());
        buf.writeInt(clipboardHash);
        buf.writeArray(data);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClipboardSendPacket)) return false;

        ClipboardSendPacket packet = (ClipboardSendPacket) o;

        if (getClipboardHash() != packet.getClipboardHash()) {
            return false;
        }
        if (!identifier.equals(packet.identifier)) {
            return false;
        }
        return Arrays.equals(getData(), packet.getData());
    }

    @Override
    public int hashCode() {
        int result = identifier.hashCode();
        result = 31 * result + getClipboardHash();
        result = 31 * result + Arrays.hashCode(getData());
        return result;
    }

    @Override
    public String toString() {
        return "ClipboardSendPacket{" +
            "identifier=" + identifier +
            ", clipboardHash=" + clipboardHash +
            ", data=" + Arrays.toString(data) +
            '}';
    }

    public int getClipboardHash() {
        return clipboardHash;
    }

    public void setClipboardHash(int clipboardHash) {
        this.clipboardHash = clipboardHash;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public void setIdentifier(UUID identifier) {
        this.identifier = identifier;
    }
}
