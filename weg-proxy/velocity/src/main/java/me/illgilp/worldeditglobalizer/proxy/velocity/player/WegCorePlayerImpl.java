package me.illgilp.worldeditglobalizer.proxy.velocity.player;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import java.util.Locale;
import java.util.UUID;
import me.illgilp.worldeditglobalizer.common.network.AbstractPacketHandler;
import me.illgilp.worldeditglobalizer.proxy.core.api.WegProxy;
import me.illgilp.worldeditglobalizer.proxy.core.api.schematic.WegSchematicContainer;
import me.illgilp.worldeditglobalizer.proxy.core.api.server.WegServerInfo;
import me.illgilp.worldeditglobalizer.proxy.core.player.WegCorePlayer;
import me.illgilp.worldeditglobalizer.proxy.core.server.connection.ServerConnection;
import me.illgilp.worldeditglobalizer.proxy.core.server.connection.ServerConnectionListener;
import me.illgilp.worldeditglobalizer.proxy.velocity.server.connection.ServerConnectionImpl;
import me.illgilp.worldeditglobalizer.proxy.velocity.util.AdventureVelocityAdapter;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;


public class WegCorePlayerImpl extends WegCorePlayer {

    private final Player player;

    public WegCorePlayerImpl(Player player, WegSchematicContainer schematicContainer) {
        super(schematicContainer);
        this.player = player;
    }

    @Override
    public UUID getUniqueId() {
        return this.player.getUniqueId();
    }

    @Override
    public String getName() {
        return this.player.getUsername();
    }

    @Override
    public boolean isOnline() {
        return this.player.isActive();
    }

    @Override
    public Component getDisplayName() {
        return LegacyComponentSerializer.legacySection().deserialize(this.player.getUsername());
    }

    @Override
    public Locale getLocale() {
        return this.player.getEffectiveLocale();
    }

    @Override
    protected WegServerInfo getServerInfo() {
        return player.getCurrentServer()
            .map(com.velocitypowered.api.proxy.ServerConnection::getServerInfo)
            .map(ServerInfo::getName)
            .flatMap(WegProxy.getInstance()::getServerInfo)
            .orElseThrow(() -> new IllegalStateException("strange state caught: could not find WegServerInfo from name of" +
                " ServerInfo the Player \"" + player + "\" is connected to"));
    }

    @Override
    protected ServerConnection getNewServerConnection(ServerConnectionListener serverConnectionListener, AbstractPacketHandler packetHandler) {
        return new ServerConnectionImpl(serverConnectionListener, packetHandler, getServerInfo(), this.player.getCurrentServer().orElse(null));
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
        AdventureVelocityAdapter.sendMessage(this.player, source, message, type);
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
        AdventureVelocityAdapter.sendActionBar(this.player, message);
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
