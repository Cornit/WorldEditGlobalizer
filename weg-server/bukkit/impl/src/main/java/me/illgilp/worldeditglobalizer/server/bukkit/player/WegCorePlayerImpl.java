package me.illgilp.worldeditglobalizer.server.bukkit.player;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import me.illgilp.worldeditglobalizer.common.network.PacketSender;
import me.illgilp.worldeditglobalizer.common.permission.Permission;
import me.illgilp.worldeditglobalizer.server.bukkit.api.worldedit.adapter.WorldEditAdapterFilter;
import me.illgilp.worldeditglobalizer.server.bukkit.server.connection.ServerConnectionImpl;
import me.illgilp.worldeditglobalizer.server.core.api.clipboard.WegClipboard;
import me.illgilp.worldeditglobalizer.server.core.player.WegCorePlayer;
import me.illgilp.worldeditglobalizer.server.core.server.connection.ServerConnection;
import me.illgilp.worldeditglobalizer.server.core.server.connection.ServerConnectionPacketHandler;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.permission.PermissionChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.util.TriState;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class WegCorePlayerImpl extends WegCorePlayer {

    private final Player player;
    private final Audience audience;
    private final Plugin plugin;
    private final PacketSender packetSender;

    public WegCorePlayerImpl(Player player, Audience audience, Plugin plugin, PacketSender packetSender) {
        this.player = player;
        this.audience = audience;
        this.plugin = plugin;
        this.packetSender = packetSender;
    }

    @Override
    protected ServerConnection getNewConnection() {
        return new ServerConnectionImpl(new ServerConnectionPacketHandler(this), player, plugin, packetSender);
    }

    @Override
    public UUID getUniqueId() {
        return this.player.getUniqueId();
    }

    @Override
    public String getName() {
        return this.player.getName();
    }

    @Override
    public Component getDisplayName() {
        return LegacyComponentSerializer.legacySection().deserialize(this.player.getDisplayName());
    }

    @Override
    public Locale getLocale() {
        return this.audience.getOrDefault(Identity.LOCALE, Locale.US);
    }

    @Override
    public Optional<WegClipboard> getClipboard() {
        return Optional.ofNullable(WorldEditAdapterFilter.getWorldEditAdapter())
            .flatMap(worldEditAdapter -> worldEditAdapter.getClipboardOfPlayer(this));
    }

    @Override
    public boolean setClipboard(byte[] data, int hashCode) {
        return Optional.ofNullable(WorldEditAdapterFilter.getWorldEditAdapter())
            .map(worldEditAdapter -> worldEditAdapter.readAndSetClipboard(this, data, hashCode))
            .orElse(false);
    }

    @Override
    public void sendMessageInternal(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
        this.audience.sendMessage(source, message, type);
    }

    @Override
    public void sendActionBarInternal(@NotNull Component message) {
        this.audience.sendActionBar(message);
    }

    @Override
    public String toString() {
        return "WegCorePlayerImpl{" +
            "uniqueId=" + getUniqueId() +
            ", name='" + getName() + '\'' +
            ", displayName='" + getDisplayName() + '\'' +
            '}';
    }

    @Override
    public TriState getPermissionValue(Permission permission) {
        return this.audience.getOrDefault(PermissionChecker.POINTER, permission1 -> TriState.NOT_SET).value(permission.getPermission());
    }
}
