package me.illgilp.worldeditglobalizer.common.network.data;

import java.io.IOException;
import java.util.UUID;

public interface PacketDataInput {

    boolean readBoolean() throws IOException;

    byte readByte() throws IOException;

    short readShort() throws IOException;

    int readInt() throws IOException;

    long readLong() throws IOException;

    float readFloat() throws IOException;

    double readDouble() throws IOException;

    int readVarInt() throws IOException;

    long readVarLong() throws IOException;

    String readString() throws IOException;

    byte[] readSizedBytes() throws IOException;

    byte[] readRawBytes(int size) throws IOException;

    byte[] readRemainingBytes() throws IOException;

    UUID readUUID() throws IOException;


}
