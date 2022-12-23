package me.illgilp.worldeditglobalizer.proxy.bungeecord.player;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import me.illgilp.worldeditglobalizer.common.network.AbstractPacketHandler;
import me.illgilp.worldeditglobalizer.proxy.bungeecord.server.connection.ServerConnectionImpl;
import me.illgilp.worldeditglobalizer.proxy.core.api.WegProxy;
import me.illgilp.worldeditglobalizer.proxy.core.api.schematic.WegSchematicContainer;
import me.illgilp.worldeditglobalizer.proxy.core.api.server.WegServerInfo;
import me.illgilp.worldeditglobalizer.proxy.core.player.WegCorePlayer;
import me.illgilp.worldeditglobalizer.proxy.core.server.connection.ServerConnection;
import me.illgilp.worldeditglobalizer.proxy.core.server.connection.ServerConnectionListener;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import org.jetbrains.annotations.NotNull;


public class WegCorePlayerImpl extends WegCorePlayer {

    private final ProxiedPlayer proxiedPlayer;
    private final Audience audience;

    public WegCorePlayerImpl(ProxiedPlayer proxiedPlayer, Audience audience, WegSchematicContainer schematicContainer) {
        super(schematicContainer);
        this.proxiedPlayer = proxiedPlayer;
        this.audience = audience;
    }

    @Override
    public UUID getUniqueId() {
        return this.proxiedPlayer.getUniqueId();
    }

    @Override
    public String getName() {
        return this.proxiedPlayer.getName();
    }

    @Override
    public boolean isOnline() {
        return this.proxiedPlayer.isConnected();
    }

    @Override
    public Component getDisplayName() {
        return LegacyComponentSerializer.legacySection().deserialize(this.proxiedPlayer.getDisplayName());
    }

    @Override
    public Locale getLocale() {
        return this.proxiedPlayer.getLocale();
    }

    @Override
    protected WegServerInfo getServerInfo() {
        return Optional.ofNullable(proxiedPlayer.getServer())
            .map(Server::getInfo)
            .map(ServerInfo::getName)
            .flatMap(WegProxy.getInstance()::getServerInfo)
            .orElseThrow(() -> new IllegalStateException("strange state caught: could not find WegServerInfo from name of" +
                " ServerInfo the Player \"" + proxiedPlayer + "\" is connected to"));
    }

    @Override
    protected ServerConnection getNewServerConnection(ServerConnectionListener serverConnectionListener, AbstractPacketHandler packetHandler) {
        return new ServerConnectionImpl(serverConnectionListener, packetHandler, getServerInfo(), this.proxiedPlayer.getServer());
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
        this.audience.sendMessage(source, message, type);
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
        this.audience.sendActionBar(message);
    }

    @Override
    public String toString() {
        return "WegCorePlayerImpl{" +
            "uniqueId=" + getUniqueId() +
            ", name='" + getName() + '\'' +
            ", online=" + isOnline() +
            ", displayName='" + getDisplayName() + '\'' +
            '}';
    }

}
