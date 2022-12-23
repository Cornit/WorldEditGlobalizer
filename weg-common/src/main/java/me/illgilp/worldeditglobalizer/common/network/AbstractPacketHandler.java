package me.illgilp.worldeditglobalizer.common.network;

import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ActionBarPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ClipboardDataPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ClipboardRequestPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.KeepAlivePacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.MessagePacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.PermissionCheckRequestPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.PermissionCheckResponsePacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ProxyConfigRequestPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ProxyConfigResponsePacket;

public abstract class AbstractPacketHandler {

    public void handle(KeepAlivePacket packet) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    public void handle(PermissionCheckRequestPacket packet) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    public void handle(PermissionCheckResponsePacket packet) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    public void handle(ProxyConfigRequestPacket packet) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    public void handle(ProxyConfigResponsePacket packet) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    public void handle(ClipboardDataPacket packet) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    public void handle(ClipboardRequestPacket packet) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    public void handle(MessagePacket packet) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    public void handle(ActionBarPacket packet) {
        throw new UnsupportedOperationException("Method not implemented");
    }
}
