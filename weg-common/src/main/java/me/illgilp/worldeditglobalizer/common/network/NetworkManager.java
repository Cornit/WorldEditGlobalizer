package me.illgilp.worldeditglobalizer.common.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import me.illgilp.worldeditglobalizer.common.ProgressListener;
import me.illgilp.worldeditglobalizer.common.WegVersion;
import me.illgilp.worldeditglobalizer.common.WorldEditGlobalizer;
import me.illgilp.worldeditglobalizer.common.network.data.stream.PacketDataInputStream;
import me.illgilp.worldeditglobalizer.common.network.data.stream.PacketDataOutputStream;
import me.illgilp.worldeditglobalizer.common.network.exception.IncompatibleVersionException;
import me.illgilp.worldeditglobalizer.common.network.exception.InvalidSignatureException;
import me.illgilp.worldeditglobalizer.common.network.exception.PacketHandleException;
import me.illgilp.worldeditglobalizer.common.network.protocol.PacketFactory;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.IdentifiedPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.Packet;
import me.illgilp.worldeditglobalizer.common.util.Signature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class NetworkManager implements Connection {

    private final Map<UUID, IncomingFragmentedPacketData> incomingFragmentedPacketDataMap = new ConcurrentHashMap<>();

    private final AbstractPacketHandler packetHandler;

    private final PacketSender packetSender;

    public NetworkManager(AbstractPacketHandler packetHandler, PacketSender packetSender) {
        this.packetHandler = Objects.requireNonNull(packetHandler, "AbstractPacketHandler cannot be null");
        this.packetSender = packetSender;
    }

    protected void handleBytes(byte[] data) {
        try {
            try (PacketDataInputStream pdis = PacketDataInputStream.forBytes(data)) {
                final WegVersion remoteVersion = new WegVersion(pdis);
                if (!remoteVersion.equals(WorldEditGlobalizer.getVersion())) {
                    throw new IncompatibleVersionException();
                }
            }
            int offset = WegVersion.BYTES_SIZE;
            final byte[] signature = new byte[32];
            System.arraycopy(data, offset, signature, 0, signature.length);
            final byte[] frameData = new byte[data.length - 32 - offset];
            System.arraycopy(data, offset + 32, frameData, 0, frameData.length);
            if (!Signature.verify(frameData, signature, getSigningSecret())) {
                throw new InvalidSignatureException();
            }
            try (final PacketDataInputStream in = PacketDataInputStream.forBytes(frameData)) {
                final DataFrame frame = new DataFrame();
                frame.read(in);
                this.handleFrame(frame);
            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new PacketHandleException(e);
        }
    }

    protected void handleFrame(DataFrame frame) throws IOException {
        final IncomingFragmentedPacketData ifpd = this.incomingFragmentedPacketDataMap.computeIfAbsent(
            frame.getFrameId(),
            uuid -> new IncomingFragmentedPacketData()
        );
        ifpd.appendFrame(frame);
        if (ifpd.isCancelled()) {
            this.incomingFragmentedPacketDataMap.remove(frame.getFrameId());
        } else {
            if (ifpd.isComplete()) {
                this.incomingFragmentedPacketDataMap.remove(frame.getFrameId());
                try (PacketDataInputStream packetIn = PacketDataInputStream.forBytes(ifpd.getPacketData())) {
                    final int packetId = packetIn.readVarInt();
                    Packet packet = getIncomingPacketFactory().createPacket(packetId);
                    packet.read(packetIn);
                    this.handlePacket(packet);
                }
            }
        }
    }

    protected void handlePacket(Packet packet) {
        if (packet instanceof IdentifiedPacket) {
            if (!((IdentifiedPacket) packet).isRequest()) {
                if (PacketCallback.callback((IdentifiedPacket) packet)) {
                    return;
                }
            }
        }
        packet.handle(this.packetHandler);
    }

    public CompletableFuture<Void> sendPacket(@NotNull Packet packet, @Nullable ProgressListener progressListener) {
        return packetSender.sendPacket(
            this,
            new OutgoingFragmentedPacketData(packet, getOutgoingPacketFactory()),
            progressListener
        );

    }

    protected void sendFrame(DataFrame dataFrame, ProgressListener progressListener, int index, int size) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        final Optional<ProgressListener> listener = Optional.ofNullable(progressListener);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte[] frameData;
        try (PacketDataOutputStream pdos = new PacketDataOutputStream(baos)) {
            dataFrame.write(pdos);
            pdos.flush();
            frameData = baos.toByteArray();
        }
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            try (PacketDataOutputStream pdos = new PacketDataOutputStream(bytes)) {
                WorldEditGlobalizer.getVersion().write(pdos);
            }
            bytes.write(Signature.sign(frameData, getSigningSecret()));
            bytes.write(frameData);
            this.sendBytes(bytes.toByteArray());
            listener.ifPresent(l -> l.onProgressChanged(((float) (index + 1) / size)));
        }
    }

    protected abstract void sendBytes(byte[] data);

    protected abstract PacketFactory getIncomingPacketFactory();

    protected abstract PacketFactory getOutgoingPacketFactory();

    protected abstract byte[] getSigningSecret();
}
