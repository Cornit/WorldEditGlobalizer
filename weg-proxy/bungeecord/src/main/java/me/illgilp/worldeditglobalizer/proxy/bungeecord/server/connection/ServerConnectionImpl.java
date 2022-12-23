package me.illgilp.worldeditglobalizer.proxy.bungeecord.server.connection;

import me.illgilp.worldeditglobalizer.common.network.AbstractPacketHandler;
import me.illgilp.worldeditglobalizer.proxy.core.api.server.WegServerInfo;
import me.illgilp.worldeditglobalizer.proxy.core.server.connection.ServerConnection;
import me.illgilp.worldeditglobalizer.proxy.core.server.connection.ServerConnectionListener;
import net.md_5.bungee.api.connection.Server;

public class ServerConnectionImpl extends ServerConnection {

    private final Server server;

    public ServerConnectionImpl(ServerConnectionListener serverConnectionListener, AbstractPacketHandler packetHandler, WegServerInfo serverInfo, Server server) {
        super(serverConnectionListener, packetHandler, serverInfo);
        this.server = server;
    }

    @Override
    protected void sendBytes(byte[] data) {
        this.server.sendData(ServerConnection.PLUGIN_MESSAGE_CHANNEL.asString(), data);
    }
}
