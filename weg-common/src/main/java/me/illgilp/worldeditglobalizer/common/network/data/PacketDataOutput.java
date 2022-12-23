package me.illgilp.worldeditglobalizer.common.network.data;

import java.io.IOException;
import java.util.UUID;

public interface PacketDataOutput {

    void writeBoolean(boolean value) throws IOException;

    void writeByte(byte value) throws IOException;

    void writeShort(short value) throws IOException;

    void writeInt(int value) throws IOException;

    void writeLong(long value) throws IOException;

    void writeFloat(float value) throws IOException;

    void writeDouble(double value) throws IOException;

    void writeVarInt(int value) throws IOException;

    void writeVarLong(long value) throws IOException;

    void writeString(String value) throws IOException;

    void writeSizedBytes(byte[] value) throws IOException;

    void writeRawBytes(byte[] value) throws IOException;


    void writeUUID(UUID value) throws IOException;


}
