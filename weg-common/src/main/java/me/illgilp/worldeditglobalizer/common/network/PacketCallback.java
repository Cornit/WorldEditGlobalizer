package me.illgilp.worldeditglobalizer.common.network;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.IdentifiedPacket;

public class PacketCallback {

    private static final Map<UUID, CallbackEntry<? extends IdentifiedPacket>> callbacks = new ConcurrentHashMap<>();

    public static <T extends IdentifiedPacket> CompletableFuture<T> request(Connection connection, IdentifiedPacket request, Class<T> responseType) {
        UUID uuid = UUID.randomUUID();
        while (callbacks.containsKey(uuid)) {
            uuid = UUID.randomUUID();
        }
        request.setId(uuid);
        request.setRequest(true);
        CompletableFuture<T> future = new CompletableFuture<>();
        callbacks.put(uuid, new SyncCallbackEntry<>(responseType, future));
        connection.sendPacket(request)
            .whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    future.completeExceptionally(throwable);
                }
            });
        return future;
    }

    protected static boolean callback(IdentifiedPacket packet) {
        if (callbacks.containsKey(packet.getId())) {
            CallbackEntry<? extends IdentifiedPacket> entry = callbacks.get(packet.getId());
            callbacks.remove(packet.getId());
            if (entry.responsePacketType.isAssignableFrom(packet.getClass())) {
                entry.call(packet);
                return true;
            }
        }
        return false;
    }


    public static void cleanup() {
        callbacks.values().removeIf(CallbackEntry::isDone);
    }

    private static abstract class CallbackEntry<O extends IdentifiedPacket> {
        private final Class<O> responsePacketType;

        public CallbackEntry(Class<O> responsePacketType) {
            this.responsePacketType = responsePacketType;
        }

        public abstract void call(IdentifiedPacket packet);

        public abstract boolean isDone();
    }

    private static final class SyncCallbackEntry<T extends IdentifiedPacket> extends CallbackEntry<T> {

        private final CompletableFuture<T> future;

        public SyncCallbackEntry(Class<T> responsePacketType, CompletableFuture<T> future) {
            super(responsePacketType);
            this.future = future;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void call(IdentifiedPacket packet) {
            this.future.complete((T) packet);
        }

        @Override
        public boolean isDone() {
            return this.future.isDone();
        }
    }
}
