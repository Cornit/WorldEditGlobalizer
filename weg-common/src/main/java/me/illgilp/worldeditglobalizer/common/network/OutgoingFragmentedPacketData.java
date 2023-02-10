package me.illgilp.worldeditglobalizer.common.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import me.illgilp.worldeditglobalizer.common.network.data.stream.PacketDataInputStream;
import me.illgilp.worldeditglobalizer.common.network.data.stream.PacketDataOutputStream;
import me.illgilp.worldeditglobalizer.common.network.protocol.PacketFactory;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.Packet;

class OutgoingFragmentedPacketData {

    private static final AtomicLong CURRENT_TIME = new AtomicLong();
    private static final AtomicLong CURRENT_FRAME = new AtomicLong();

    @Getter
    private final Packet packet;
    private final Queue<DataFrame> frames = new ConcurrentLinkedQueue<>();
    private final PacketFactory outgoingPacketFactory;
    private boolean generated = false;
    private UUID frameId;
    private int size;

    public OutgoingFragmentedPacketData(Packet packet, PacketFactory outgoingPacketFactory) {
        this.packet = packet;
        this.outgoingPacketFactory = outgoingPacketFactory;
    }

    private static UUID nextFrameId() {
        synchronized (CURRENT_TIME) {
            final long now = System.currentTimeMillis();
            if (CURRENT_TIME.get() < now) {
                CURRENT_TIME.set(now);
                CURRENT_FRAME.set(0);
            }
            return new UUID(CURRENT_TIME.get(), CURRENT_FRAME.getAndIncrement());
        }
    }

    public Optional<DataFrame> nextFrame() throws IOException {
        if (!this.generated) {
            generateFrames();
        }
        return Optional.ofNullable(this.frames.poll());
    }

    private void generateFrames() throws IOException {
        if (this.generated) {
            return;
        }
        this.generated = true;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PacketDataOutputStream pdos = new PacketDataOutputStream(baos);
        pdos.writeVarInt(this.outgoingPacketFactory.getPacketId(this.packet.getClass()));
        this.packet.write(pdos);
        pdos.flush();
        pdos.close();
        this.frameId = nextFrameId();
        final PacketDataInputStream pdis = PacketDataInputStream.forBytes(baos.toByteArray());
        while (pdis.available() > 0) {
            final int framePayloadSize = Math.min(DataFrame.MAX_FRAME_PAYLOAD_SIZE, pdis.available());
            final byte[] data = pdis.readRawBytes(framePayloadSize);
            this.frames.add(new DataFrame(this.frameId, pdis.available() > 0, false, data));
        }
        pdis.close();
        this.size = this.frames.size();
    }

    public boolean prepare() throws IOException {
        if (this.generated) {
            return false;
        }
        generateFrames();
        return true;
    }

    public int size() {
        return this.size;
    }

    public int remaining() {
        return this.frames.size();
    }

    public DataFrame getCancelFrame() {
        return new DataFrame(this.frameId, false, true, new byte[] {});
    }
}
