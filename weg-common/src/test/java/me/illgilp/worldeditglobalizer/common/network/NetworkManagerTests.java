package me.illgilp.worldeditglobalizer.common.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.illgilp.worldeditglobalizer.common.WegManifest;
import me.illgilp.worldeditglobalizer.common.WegVersion;
import me.illgilp.worldeditglobalizer.common.WorldEditGlobalizer;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataInput;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataOutput;
import me.illgilp.worldeditglobalizer.common.network.protocol.PacketFactory;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.Packet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NetworkManagerTests {

    @BeforeEach
    public void init() {
        try {
            Field weg_manifest = WorldEditGlobalizer.class.getDeclaredField("WEG_MANIFEST");
            weg_manifest.setAccessible(true);
            weg_manifest.set(null, new WegManifest(new WegVersion(3, 0, 0, WegVersion.Type.BETA), "(unknown)"));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test_NetworkManager_send_handle_KeepAlive() {
        final List<byte[]> pdata = new ArrayList<>();
        final FullPacket[] received = { null };
        final PacketFactory packetFactory = new PacketFactory() {
            @Override
            public Packet createPacket(int id) {
                if (id == 1) {
                    return new FullPacket();
                }
                return null;
            }

            @Override
            public int getPacketId(Class<? extends Packet> packet) {
                return FullPacket.class.isAssignableFrom(packet) ? 1 : 0;
            }
        };

        final NetworkManager manager = new NetworkManager(new TestPacketHandler() {
            @Override
            public void handle(FullPacket packet) {
                received[0] = packet;
            }
        }) {
            @Override
            protected void sendBytes(byte[] data) {
                pdata.add(data);
            }

            @Override
            protected PacketFactory getIncomingPacketFactory() {
                return packetFactory;
            }

            @Override
            protected PacketFactory getOutgoingPacketFactory() {
                return packetFactory;
            }

            @Override
            protected byte[] getSigningSecret() {
                return "Secret".getBytes(StandardCharsets.UTF_8);
            }

            @Override
            protected void scheduleFrameSend(Runnable runnable, int index) {
                runnable.run();
            }
        };
        final Random random = new Random();
        final byte[] randomByte = new byte[1];
        random.nextBytes(randomByte);
        final byte[] randomSized = new byte[random.nextInt(1, Short.MAX_VALUE)];
        random.nextBytes(randomSized);
        final byte[] randomRaw = new byte[10];
        random.nextBytes(randomRaw);
        final byte[] randomRemain = new byte[random.nextInt(1, Short.MAX_VALUE)];
        random.nextBytes(randomRemain);
        final byte[] randomString = new byte[random.nextInt(1, Short.MAX_VALUE)];
        random.nextBytes(randomString);
        final FullPacket fullPacket =
            FullPacket.builder()
                .bool(random.nextBoolean())
                .byt(randomByte[0])
                .sho((short) random.nextInt(Short.MIN_VALUE, Short.MAX_VALUE + 1))
                .intt(random.nextInt())
                .lon(random.nextLong())
                .floa(random.nextFloat())
                .doubl(random.nextDouble())
                .varInt(random.nextInt())
                .varLong(random.nextLong())
                .string(new String(randomString, StandardCharsets.UTF_8))
                .uuid(UUID.randomUUID())
                .sizedBytes(randomSized)
                .rawBytes(randomRaw)
                .rawRemaining(randomRemain)
                .build();
        manager.sendPacket(fullPacket, null);
        assertNotEquals(0, pdata.size());
        pdata.forEach(manager::handleBytes);
        assertNotNull(received[0]);
        assertEquals(fullPacket, received[0]);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    @Builder
    private static class FullPacket extends Packet {

        private boolean bool;
        private byte byt;
        private short sho;
        private int intt;
        private long lon;
        private float floa;
        private double doubl;
        private int varInt;
        private long varLong;
        private String string;
        private UUID uuid;
        private byte[] sizedBytes;
        private byte[] rawBytes;
        private byte[] rawRemaining;


        @Override
        public void read(PacketDataInput in) throws IOException {
            this.bool = in.readBoolean();
            this.byt = in.readByte();
            this.sho = in.readShort();
            this.intt = in.readInt();
            this.lon = in.readLong();
            this.floa = in.readFloat();
            this.doubl = in.readDouble();
            this.varInt = in.readVarInt();
            this.varLong = in.readVarLong();
            this.string = in.readString();
            this.uuid = in.readUUID();
            this.sizedBytes = in.readSizedBytes();
            this.rawBytes = in.readRawBytes(10);
            this.rawRemaining = in.readRemainingBytes();
        }

        @Override
        public void write(PacketDataOutput out) throws IOException {
            out.writeBoolean(this.bool);
            out.writeByte(this.byt);
            out.writeShort(this.sho);
            out.writeInt(this.intt);
            out.writeLong(this.lon);
            out.writeFloat(this.floa);
            out.writeDouble(this.doubl);
            out.writeVarInt(this.varInt);
            out.writeVarLong(this.varLong);
            out.writeString(this.string);
            out.writeUUID(this.uuid);
            out.writeSizedBytes(this.sizedBytes);
            out.writeRawBytes(this.rawBytes);
            out.writeRawBytes(this.rawRemaining);
        }

        @Override
        public void handle(AbstractPacketHandler packetHandler) {
            if (packetHandler instanceof TestPacketHandler) {
                ((TestPacketHandler) packetHandler).handle(this);
            }
        }
    }

    private static class TestPacketHandler extends AbstractPacketHandler {

        public void handle(FullPacket packet) {
            throw new UnsupportedOperationException("Method not implemented");
        }

    }

}
