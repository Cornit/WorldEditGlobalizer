package me.illgilp.worldeditglobalizer.common.network.protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import lombok.Getter;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ActionBarPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.AutoUploadReadyPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ClipboardDataPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ClipboardRequestPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.KeepAlivePacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.MessagePacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.Packet;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.PermissionCheckRequestPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.PermissionCheckResponsePacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ProxyConfigRequestPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ProxyConfigResponsePacket;

public enum Protocol {

    TO_PROXY(Direction.TO_PROXY) {
        {
            registerPacket(0x0, KeepAlivePacket.class, KeepAlivePacket::new);
            registerPacket(0x1, PermissionCheckResponsePacket.class, PermissionCheckResponsePacket::new);
            registerPacket(0x2, ProxyConfigRequestPacket.class, ProxyConfigRequestPacket::new);
            registerPacket(0x3, ClipboardDataPacket.class, ClipboardDataPacket::new);
            registerPacket(0x4, MessagePacket.class, MessagePacket::new);
            registerPacket(0x5, ActionBarPacket.class, ActionBarPacket::new);
        }
    },

    TO_SERVER(Direction.TO_SERVER) {
        {
            registerPacket(0x0, KeepAlivePacket.class, KeepAlivePacket::new);
            registerPacket(0x1, PermissionCheckRequestPacket.class, PermissionCheckRequestPacket::new);
            registerPacket(0x2, ProxyConfigResponsePacket.class, ProxyConfigResponsePacket::new);
            registerPacket(0x3, ClipboardDataPacket.class, ClipboardDataPacket::new);
            registerPacket(0x4, ClipboardRequestPacket.class, ClipboardRequestPacket::new);
            registerPacket(0x5, AutoUploadReadyPacket.class, AutoUploadReadyPacket::new);
        }
    };

    @Getter
    private final Direction direction;

    private final Map<Integer, Supplier<? extends Packet>> idToPacketMap = new ConcurrentHashMap<>();
    private final Map<Class<? extends Packet>, Integer> packetToIdMap = new ConcurrentHashMap<>();

    Protocol(Direction direction) {
        this.direction = direction;
    }

    <T extends Packet> void registerPacket(int id, Class<T> packet, Supplier<T> constructor) {
        if (idToPacketMap.containsKey(id)) {
            throw new IllegalStateException("packet with id " + id + " already registered");
        }
        idToPacketMap.put(id, constructor);
        packetToIdMap.put(packet, id);
    }

    public Packet createPacket(int id) {
        final Supplier<? extends Packet> constructor = idToPacketMap.get(id);
        return constructor == null ? null : constructor.get();
    }

    public int getId(Class<? extends Packet> packet) {
        return packetToIdMap.get(packet);
    }

    public enum Direction {
        TO_PROXY,
        TO_SERVER
    }
}
