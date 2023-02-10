package me.illgilp.worldeditglobalizer.server.bukkit.server.connection;

import me.illgilp.worldeditglobalizer.common.network.AbstractPacketHandler;
import me.illgilp.worldeditglobalizer.common.network.PacketSender;
import me.illgilp.worldeditglobalizer.server.core.server.connection.ServerConnection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ServerConnectionImpl extends ServerConnection {

    private final Player player;
    private final Plugin plugin;

    public ServerConnectionImpl(AbstractPacketHandler packetHandler, Player player, Plugin plugin, PacketSender packetSender) {
        super(packetHandler, packetSender);
        this.player = player;
        this.plugin = plugin;
    }

    @Override
    protected void sendBytes(byte[] data) {
        this.player.sendPluginMessage(this.plugin, ServerConnection.PLUGIN_MESSAGE_CHANNEL.asString(), data);
    }

    @Override
    public boolean isConnected() {
        return this.player.isOnline();
    }
}
