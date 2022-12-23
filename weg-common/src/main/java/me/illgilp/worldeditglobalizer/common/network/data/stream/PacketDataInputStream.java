package me.illgilp.worldeditglobalizer.common.network.data.stream;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataInput;

public class PacketDataInputStream extends InputStream implements PacketDataInput {

    private final DataInputStream in;


    public PacketDataInputStream(InputStream in) {
        if (in instanceof DataInputStream) {
            this.in = (DataInputStream) in;
        } else {
            this.in = new DataInputStream(in);
        }
    }

    public static PacketDataInputStream forBytes(byte[] data) {
        return new PacketDataInputStream(new ByteArrayInputStream(data));
    }

    @Override
    public int read() throws IOException {
        return this.in.read();
    }

    @Override
    public boolean readBoolean() throws IOException {
        return this.in.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return this.in.readByte();
    }

    @Override
    public short readShort() throws IOException {
        return this.in.readShort();
    }

    @Override
    public int readInt() throws IOException {
        return this.in.readInt();
    }

    @Override
    public long readLong() throws IOException {
        return this.in.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return this.in.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return this.in.readDouble();
    }

    @Override
    public int readVarInt() throws IOException {
        int numRead = 0;
        int result = 0;
        byte read;
        while (true) {
            read = this.readByte();
            result |= (read & 0x7F) << (numRead++ * 7);
            if (numRead > 5) {
                throw new RuntimeException("VarInt too big");
            }
            if ((read & 0x80) != 0x80) {
                break;
            }
        }
        return result;
    }

    @Override
    public long readVarLong() throws IOException {
        int numRead = 0;
        long result = 0;
        byte read;
        do {
            read = this.readByte();
            int value = (read & 0b01111111);
            result |= ((long) value << (7 * numRead));
            numRead++;
            if (numRead > 10) {
                throw new RuntimeException("VarLong too big");
            }
        } while ((read & 0b10000000) != 0);
        return result;
    }

    @Override
    public String readString() throws IOException {
        return new String(this.readSizedBytes(), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] readSizedBytes() throws IOException {
        return this.readRawBytes(this.readVarInt());
    }

    @Override
    public byte[] readRawBytes(int size) throws IOException {
        final byte[] data = new byte[size];
        this.in.readFully(data);
        return data;
    }

    @Override
    public byte[] readRemainingBytes() throws IOException {
        return readRawBytes(this.in.available());
    }

    @Override
    public UUID readUUID() throws IOException {
        final long mostSignificant = this.readVarLong();
        final long leastSignificant = this.readVarLong();
        return new UUID(mostSignificant, leastSignificant);
    }

    @Override
    public int available() throws IOException {
        return this.in.available();
    }
}
