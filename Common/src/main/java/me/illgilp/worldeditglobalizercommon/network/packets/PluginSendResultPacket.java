package me.illgilp.worldeditglobalizercommon.network.packets;

import java.util.UUID;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;

public class PluginSendResultPacket extends Packet {

    private UUID identifier = UUID.randomUUID();

    private int tryNum;
    private Result result;

    public PluginSendResultPacket() {
    }

    public PluginSendResultPacket(UUID identifier, int tryNum, Result result) {
        this.identifier = identifier;
        this.tryNum = tryNum;
        this.result = result;
    }

    @Override
    public void read(PacketDataSerializer buf) {
        identifier = buf.readUUID();
        tryNum = buf.readVarInt();
        result = Result.values()[buf.readVarInt()];
    }

    @Override
    public void write(PacketDataSerializer buf) {
        buf.writeUUID(identifier);
        buf.writeVarInt(tryNum);
        buf.writeVarInt(result.ordinal());
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public void setIdentifier(UUID identifier) {
        this.identifier = identifier;
    }

    public int getTryNum() {
        return tryNum;
    }

    public void setTryNum(int tryNum) {
        this.tryNum = tryNum;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PluginSendResultPacket that = (PluginSendResultPacket) o;

        if (tryNum != that.tryNum) {
            return false;
        }
        if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) {
            return false;
        }
        return result == that.result;
    }

    @Override
    public int hashCode() {
        int result1 = identifier != null ? identifier.hashCode() : 0;
        result1 = 31 * result1 + tryNum;
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        return result1;
    }

    @Override
    public String toString() {
        return "PluginSendResultPacket{" +
            "identifier=" + identifier +
            ", tryNum=" + tryNum +
            ", result=" + result +
            '}';
    }

    public enum Result {
        FAILED,
        SUCCESS
    }
}
