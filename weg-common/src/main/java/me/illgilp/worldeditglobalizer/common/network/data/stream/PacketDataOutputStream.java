package me.illgilp.worldeditglobalizer.common.network.data.stream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataOutput;

public class PacketDataOutputStream extends OutputStream implements PacketDataOutput {

    private final DataOutputStream out;

    public PacketDataOutputStream(OutputStream out) {
        if (out instanceof DataOutputStream) {
            this.out = (DataOutputStream) out;
        } else {
            this.out = new DataOutputStream(out);
        }
    }


    @Override
    public void writeBoolean(boolean value) throws IOException {
        this.write(value ? 1 : 0);
    }

    @Override
    public void writeByte(byte value) throws IOException {
        this.write(value);
    }

    @Override
    public void writeShort(short value) throws IOException {
        this.out.writeShort(value);
    }

    @Override
    public void writeInt(int value) throws IOException {
        this.out.writeInt(value);
    }

    @Override
    public void writeLong(long value) throws IOException {
        this.out.writeLong(value);
    }

    @Override
    public void writeFloat(float value) throws IOException {
        this.out.writeFloat(value);
    }

    @Override
    public void writeDouble(double value) throws IOException {
        this.out.writeDouble(value);
    }

    @Override
    public void writeVarInt(int value) throws IOException {
        int part;
        while (true) {
            part = value & 0x7F;
            value >>>= 7;
            if (value != 0) {
                part |= 0x80;
            }
            this.write(part);
            if (value == 0) {
                break;
            }
        }
    }

    @Override
    public void writeVarLong(long value) throws IOException {
        do {
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            this.writeByte(temp);
        } while (value != 0);
    }

    @Override
    public void writeString(String value) throws IOException {
        this.writeSizedBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void writeSizedBytes(byte[] value) throws IOException {
        this.writeVarInt(value.length);
        this.writeRawBytes(value);
    }

    @Override
    public void writeRawBytes(byte[] value) throws IOException {
        this.out.write(value);
    }

    @Override
    public void writeUUID(UUID value) throws IOException {
        this.writeVarLong(value.getMostSignificantBits());
        this.writeVarLong(value.getLeastSignificantBits());
    }

    @Override
    public void write(int value) throws IOException {
        this.out.write(value);
    }

    @Override
    public void flush() throws IOException {
        this.out.flush();
    }

    @Override
    public void close() throws IOException {
        this.out.close();
    }
}
