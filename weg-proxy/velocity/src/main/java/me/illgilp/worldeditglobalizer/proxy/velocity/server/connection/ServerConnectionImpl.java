package me.illgilp.worldeditglobalizer.proxy.velocity.server.connection;

import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import me.illgilp.worldeditglobalizer.common.network.AbstractPacketHandler;
import me.illgilp.worldeditglobalizer.common.network.PacketSender;
import me.illgilp.worldeditglobalizer.proxy.core.api.server.WegServerInfo;
import me.illgilp.worldeditglobalizer.proxy.core.server.connection.ServerConnection;
import me.illgilp.worldeditglobalizer.proxy.core.server.connection.ServerConnectionListener;

public class ServerConnectionImpl extends ServerConnection {

    private final com.velocitypowered.api.proxy.ServerConnection serverConnection;

    public ServerConnectionImpl(
        ServerConnectionListener serverConnectionListener,
        AbstractPacketHandler packetHandler,
        WegServerInfo serverInfo,
        com.velocitypowered.api.proxy.ServerConnection serverConnection,
        PacketSender packetSender
    ) {
        super(serverConnectionListener, packetHandler, serverInfo, packetSender);
        this.serverConnection = serverConnection;
    }

    @Override
    protected void sendBytes(byte[] data) {
        this.serverConnection.sendPluginMessage(MinecraftChannelIdentifier.from(ServerConnection.PLUGIN_MESSAGE_CHANNEL.asString()), data);
    }

    @Override
    public boolean isConnected() {
        return serverConnection.getPlayer().getCurrentServer()
            .map(currentServer -> currentServer.equals(this.serverConnection))
            .orElse(false);
    }
}
