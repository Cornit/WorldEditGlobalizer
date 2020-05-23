package me.illgilp.worldeditglobalizercommon.network.packets;


import java.util.Arrays;
import java.util.UUID;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;

public class ClipboardSendPacket extends Packet {

    private UUID identifier = UUID.randomUUID();
    private int clipboardHash;
    private byte[] data;
    private Action action = Action.SEND;

    @Override
    public void read(PacketDataSerializer buf) {
        identifier = UUID.fromString(buf.readString());
        clipboardHash = buf.readInt();
        data = buf.readArray();
        action = Action.values()[buf.readVarInt()];
    }

    @Override
    public void write(PacketDataSerializer buf) {
        buf.writeString(identifier.toString());
        buf.writeInt(clipboardHash);
        buf.writeArray(data);
        buf.writeVarInt(action.ordinal());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClipboardSendPacket that = (ClipboardSendPacket) o;

        if (clipboardHash != that.clipboardHash) {
            return false;
        }
        if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) {
            return false;
        }
        if (!Arrays.equals(data, that.data)) {
            return false;
        }
        return action == that.action;
    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + clipboardHash;
        result = 31 * result + Arrays.hashCode(data);
        result = 31 * result + (action != null ? action.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClipboardSendPacket{" +
            "identifier=" + identifier +
            ", clipboardHash=" + clipboardHash +
            ", action=" + action +
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

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public enum Action {
        SEND,
        CLEAR,
        TOO_BIG
    }
}
