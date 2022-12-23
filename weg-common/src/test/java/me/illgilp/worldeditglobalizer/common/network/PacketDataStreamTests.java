package me.illgilp.worldeditglobalizer.common.network;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataInput;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataOutput;
import me.illgilp.worldeditglobalizer.common.network.data.stream.PacketDataInputStream;
import me.illgilp.worldeditglobalizer.common.network.data.stream.PacketDataOutputStream;
import me.illgilp.worldeditglobalizer.common.util.CatchingConsumer;
import me.illgilp.worldeditglobalizer.common.util.CatchingFunction;
import org.junit.jupiter.api.Test;

public class PacketDataStreamTests {

    private static final byte[] VARINT_MAX_VALUE = new byte[] {
        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x7
    };
    private static final byte[] VARINT_MIN_VALUE = new byte[] {
        (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x8
    };
    private static final byte[] VARLONG_MAX_VALUE = new byte[] {
        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x7f
    };
    private static final byte[] VARLONG_MIN_VALUE = new byte[] {
        (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80,
        (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x1
    };
    private static final byte[] STRING_HELLO = new byte[] {
        (byte) 0b00000101, 'H', 'e', 'l', 'l', 'o'
    };

    @Test
    public void test_PacketDataOutput_writeVarInt() throws IOException {
        assertArrayEquals(
            getBytes(p -> p.writeVarInt(Integer.MAX_VALUE)),
            VARINT_MAX_VALUE
        );
        assertArrayEquals(
            getBytes(p -> p.writeVarInt(Integer.MIN_VALUE)),
            VARINT_MIN_VALUE
        );
    }

    @Test
    public void test_PacketDataOutput_writeVarLong() throws IOException {
        assertArrayEquals(
            getBytes(p -> p.writeVarLong(Long.MAX_VALUE)),
            VARLONG_MAX_VALUE
        );
        assertArrayEquals(
            getBytes(p -> p.writeVarLong(Long.MIN_VALUE)),
            VARLONG_MIN_VALUE
        );
    }

    @Test
    public void test_PacketDataOutput_writeString() throws IOException {
        assertArrayEquals(
            getBytes(p -> p.writeString("Hello")),
            STRING_HELLO
        );
    }

    @Test
    public void test_PacketDataInput_readVarInt() throws IOException {
        assertEquals(
            (int) getValue(VARINT_MAX_VALUE, PacketDataInput::readVarInt),
            Integer.MAX_VALUE
        );
        assertEquals(
            (int) getValue(VARINT_MIN_VALUE, PacketDataInput::readVarInt),
            Integer.MIN_VALUE
        );
    }

    @Test
    public void test_PacketDataInput_readVarLong() throws IOException {
        assertEquals(
            (long) getValue(VARLONG_MAX_VALUE, PacketDataInput::readVarLong),
            Long.MAX_VALUE
        );
        assertEquals(
            (long) getValue(VARLONG_MIN_VALUE, PacketDataInput::readVarLong),
            Long.MIN_VALUE
        );
    }

    @Test
    public void test_PacketDataInput_readString() throws IOException {
        assertEquals(
            getValue(STRING_HELLO, PacketDataInput::readString),
            "Hello"
        );
    }

    private byte[] getBytes(CatchingConsumer<PacketDataOutput, IOException> write) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PacketDataOutputStream po = new PacketDataOutputStream(out);
        write.accept(po);
        po.flush();
        po.close();
        return out.toByteArray();
    }

    private <T> T getValue(byte[] data, CatchingFunction<PacketDataInput, T, IOException> function) throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(data);
        final PacketDataInputStream pi = new PacketDataInputStream(in);
        T val = function.accept(pi);
        pi.close();
        return val;
    }

}
