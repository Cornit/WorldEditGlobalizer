package me.illgilp.worldeditglobalizersponge.network.packets;




import me.illgilp.worldeditglobalizersponge.util.PacketDataSerializer;

import java.util.Arrays;
import java.util.UUID;

public class ClipboardSendPacket extends Packet {

    private UUID identifier = UUID.randomUUID();
    private int clipboardhash;
    private byte[] data;

    @Override
    public void read(PacketDataSerializer buf) {
        identifier = UUID.fromString(buf.readString());
        clipboardhash = buf.readInt();
        data = buf.readArray();
    }

    @Override
    public void write(PacketDataSerializer buf) {
        buf.writeString(identifier.toString());
        buf.writeInt(clipboardhash);
        buf.writeArray(data);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClipboardSendPacket)) return false;

        ClipboardSendPacket packet = (ClipboardSendPacket) o;

        if (getClipboardhash() != packet.getClipboardhash()) return false;
        if (!identifier.equals(packet.identifier)) return false;
        return Arrays.equals(getData(), packet.getData());
    }

    @Override
    public int hashCode() {
        int result = identifier.hashCode();
        result = 31 * result + getClipboardhash();
        result = 31 * result + Arrays.hashCode(getData());
        return result;
    }

    @Override
    public String toString() {
        return "ClipboardSendPacket{" +
                "identifier=" + identifier +
                ", clipboardhash=" + clipboardhash +
                ", data=" + Arrays.toString(data) +
                '}';
    }

    public int getClipboardhash() {
        return clipboardhash;
    }

    public void setClipboardhash(int clipboardhash) {
        this.clipboardhash = clipboardhash;
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
