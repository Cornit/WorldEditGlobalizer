package me.illgilp.worldeditglobalizer.common.network;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import me.illgilp.worldeditglobalizer.common.ProgressListener;
import me.illgilp.worldeditglobalizer.common.network.exception.PacketSendException;
import me.illgilp.worldeditglobalizer.common.scheduler.WegScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PacketSender {
    private final Map<NetworkManager, Queue<QueuedPacket>> packetQueues = new ConcurrentHashMap<>();

    private boolean running = false;

    public void start() {
        if (running) {
            return;
        }
        WegScheduler.getInstance().getAsyncPacketWriteExecutor()
            .scheduleAtFixedRate(this::run, 100, 100, TimeUnit.MILLISECONDS);
        this.running = true;
    }

    public CompletableFuture<Void> sendPacket(NetworkManager networkManager, OutgoingFragmentedPacketData ofpd, @Nullable ProgressListener progressListener) {
        final CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        WegScheduler.getInstance().getAsyncPacketWriteExecutor().execute(() -> {
            try {
                ofpd.prepare();
            } catch (IOException e) {
                completableFuture.completeExceptionally(
                    new PacketSendException("Failed to prepare outgoing fragmented packet data", e)
                );
                return;
            }
            if (ofpd.getPacket().canBeSentImmediately()) {
                try {
                    Optional<DataFrame> frame;
                    int index = 0;
                    while ((frame = ofpd.nextFrame()).isPresent()) {
                        networkManager.sendFrame(frame.get(), progressListener, index++, ofpd.size());
                    }
                    completableFuture.complete(null);
                    return;
                } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                    completableFuture.completeExceptionally(
                        new PacketSendException("Failed to send packet", e)
                    );
                    return;
                }
            }
            Queue<QueuedPacket> queue = getOrCreateQueue(networkManager);
            queue.stream()
                .filter(packet -> ofpd.getPacket().canCancelPacketSending(packet.ofpd.getPacket()))
                .forEach(queuedPacket -> queuedPacket.cancelled = true);
            queue.add(new QueuedPacket(networkManager, ofpd, progressListener, completableFuture));
        });
        return completableFuture;
    }

    private void run() {
        List<QueuedPacket> nextPackets = new ArrayList<>();
        for (Map.Entry<NetworkManager, Queue<QueuedPacket>> entry : new ArrayList<>(packetQueues.entrySet())) {
            if (!entry.getKey().isConnected()) {
                entry.getValue().forEach(queuedPacket -> queuedPacket.completableFuture.cancel(true));
                packetQueues.remove(entry.getKey());
            } else {
                QueuedPacket queuedPacket;
                do {
                    queuedPacket = entry.getValue().poll();
                    if (queuedPacket != null) {
                        if (queuedPacket.cancelled) {
                            try {
                                queuedPacket.networkManager.sendFrame(queuedPacket.ofpd.getCancelFrame(), null, 0, 0);
                                queuedPacket.completableFuture.cancel(true);
                            } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                                queuedPacket.completableFuture.completeExceptionally(
                                    new PacketSendException("Failed to send cancel data frame", e)
                                );
                            }
                            queuedPacket = null;
                        } else {
                            if (queuedPacket.ofpd.remaining() == 0) {
                                queuedPacket.completableFuture.complete(null);
                                queuedPacket = null;
                            }
                        }
                    }
                } while (queuedPacket == null && !entry.getValue().isEmpty());
                if (queuedPacket != null) {
                    nextPackets.add(queuedPacket);
                }
            }
        }
        nextPackets.sort(QueuedPacket::compareTo);
        if (nextPackets.size() > 0) {
            QueuedPacket packet = nextPackets.get(0);
            try {
                final int index = packet.ofpd.size() - packet.ofpd.remaining();
                Optional<DataFrame> frame = packet.ofpd.nextFrame();
                if (frame.isPresent()) {
                    packet.networkManager.sendFrame(frame.get(), packet.progressListener, index, packet.ofpd.size());
                }
            } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                packet.completableFuture.completeExceptionally(
                    new PacketSendException("Failed to send data frame", e)
                );
            }
        }
        for (QueuedPacket nextPacket : nextPackets) {
            if (nextPacket.cancelled && !nextPacket.completableFuture.isCancelled()) {
                nextPacket.completableFuture.cancel(true);
            }
            if (nextPacket.ofpd.remaining() == 0 && !nextPacket.completableFuture.isDone()) {
                nextPacket.completableFuture.complete(null);
            }
            if (!nextPacket.cancelled && nextPacket.ofpd.remaining() > 0) {
                getOrCreateQueue(nextPacket.networkManager)
                    .add(nextPacket);
            }
        }
    }

    private Queue<QueuedPacket> getOrCreateQueue(NetworkManager networkManager) {
        return packetQueues.computeIfAbsent(networkManager, manager -> new ConcurrentLinkedQueue<>());
    }


    @RequiredArgsConstructor
    @ToString
    private static final class QueuedPacket implements Comparable<QueuedPacket> {
        private final NetworkManager networkManager;
        private final OutgoingFragmentedPacketData ofpd;
        private final ProgressListener progressListener;
        private final CompletableFuture<Void> completableFuture;
        private boolean cancelled;

        @Override
        public int compareTo(@NotNull PacketSender.QueuedPacket o) {
            return this.ofpd.remaining() - o.ofpd.remaining();
        }
    }

}
