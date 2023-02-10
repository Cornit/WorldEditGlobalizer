package me.illgilp.worldeditglobalizer.common.network;

import java.util.concurrent.CompletableFuture;
import me.illgilp.worldeditglobalizer.common.ProgressListener;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.Packet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Connection {


    default CompletableFuture<Void> sendPacket(@NotNull Packet packet) {
        return this.sendPacket(packet, null);
    }

    CompletableFuture<Void> sendPacket(@NotNull Packet packet, @Nullable ProgressListener progressListener);

    boolean isConnected();

}
