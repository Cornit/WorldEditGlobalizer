package me.illgilp.worldeditglobalizer.common.network;

import me.illgilp.worldeditglobalizer.common.ProgressListener;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.Packet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Connection {


    default void sendPacket(@NotNull Packet packet) {
        this.sendPacket(packet, null);
    }

    void sendPacket(@NotNull Packet packet, @Nullable ProgressListener progressListener);

}
