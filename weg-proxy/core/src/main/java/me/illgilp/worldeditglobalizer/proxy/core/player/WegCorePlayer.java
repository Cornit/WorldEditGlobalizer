package me.illgilp.worldeditglobalizer.proxy.core.player;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.messages.tag.ProgressBarTag;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;
import me.illgilp.worldeditglobalizer.common.network.AbstractPacketHandler;
import me.illgilp.worldeditglobalizer.common.network.PacketCallback;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ClipboardDataPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ClipboardRequestPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.PermissionCheckRequestPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.PermissionCheckResponsePacket;
import me.illgilp.worldeditglobalizer.common.permission.Permission;
import me.illgilp.worldeditglobalizer.proxy.core.api.WegProxy;
import me.illgilp.worldeditglobalizer.proxy.core.api.clipboard.WegClipboard;
import me.illgilp.worldeditglobalizer.proxy.core.api.clipboard.WegClipboardContainer;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegPlayer;
import me.illgilp.worldeditglobalizer.proxy.core.api.schematic.WegSchematicContainer;
import me.illgilp.worldeditglobalizer.proxy.core.api.server.ServerNotUsableException;
import me.illgilp.worldeditglobalizer.proxy.core.api.server.WegServer;
import me.illgilp.worldeditglobalizer.proxy.core.api.server.WegServerInfo;
import me.illgilp.worldeditglobalizer.proxy.core.server.connection.ServerConnection;
import me.illgilp.worldeditglobalizer.proxy.core.server.connection.ServerConnectionListener;
import me.illgilp.worldeditglobalizer.proxy.core.server.connection.ServerConnectionPacketHandler;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;

public abstract class WegCorePlayer implements WegPlayer {

    private final Identity identity = new IdentityImpl();
    private final ServerConnectionPacketHandler serverConnectionPacketHandler;
    private final ServerConnectionListener serverConnectionListener;
    private WegClipboardContainer clipboardContainer;
    private ServerConnection serverConnection;

    private final WegSchematicContainer schematicContainer;

    private long lastPermissionRequest = 0;

    public WegCorePlayer(WegSchematicContainer schematicContainer) {
        this.schematicContainer = schematicContainer;
        this.serverConnectionPacketHandler = new ServerConnectionPacketHandler(this);
        this.serverConnectionListener = new ServerConnectionListener(this);
    }

    public abstract Component getDisplayName();

    public abstract Locale getLocale();

    public WegServer getServer() {
        return getServerConnection();
    }

    @Override
    public WegClipboardContainer getClipboardContainer() {
        return clipboardContainer == null ? (clipboardContainer = new WegClipboardContainer(this.getUniqueId())) : clipboardContainer;
    }

    @Override
    public WegSchematicContainer getSchematicContainer() {
        return this.schematicContainer;
    }

    @Override
    public void requestClipboardUpload() throws ServerNotUsableException {
        if (!this.getServer().isUsable()) {
            throw new ServerNotUsableException(this.getServer());
        }
        getServer().sendPacket(new ClipboardRequestPacket());
    }

    @Override
    public boolean downloadClipboard() throws ServerNotUsableException {
        if (!this.getServer().isUsable()) {
            throw new ServerNotUsableException(this.getServer());
        }
        if (!getClipboardContainer().hasClipboard()) {
            return false;
        }
        Optional<WegClipboard> clipboard = getClipboardContainer().getClipboard();
        if (!clipboard.isPresent()) {
            return false;
        }
        try {
            final byte[] data = clipboard.get().getData();
            int hash = clipboard.get().getHash();
            MessageHelper.builder()
                .translation(TranslationKey.CLIPBOARD_DOWNLOAD_START)
                .tagResolver(Placeholder.unparsed("clipboard_size", BigDecimal.valueOf(data.length).toPlainString()))
                .sendMessageTo(this);
            getServerConnection().sendPacket(
                new ClipboardDataPacket(hash, data),
                percentage -> {
                    String percFormatted = new DecimalFormat("000.0").format(percentage * 100.0);
                    MessageHelper.builder()
                        .translation(TranslationKey.CLIPBOARD_DOWNLOAD_PROGRESS)
                        .tagResolver(TagResolver.resolver("progressbar", (argumentQueue, context) ->
                            ProgressBarTag.create(percentage, argumentQueue, context)))
                        .tagResolver(Placeholder.unparsed("percentage", percFormatted))
                        .sendActionBarTo(this);
                }
            );
            return true;
        } catch (IOException e) {
            MessageHelper.builder()
                .translation(TranslationKey.CLIPBOARD_DOWNLOAD_ERROR)
                .sendMessageTo(this);
            WegProxy.getInstance().getLogger()
                .log(Level.SEVERE, "Exception while downloading clipboard of player '" + this.getName() + "'", e);
            return true;
        }
    }

    public ServerConnection getServerConnection() {
        if (serverConnection != null) {
            if (serverConnection.getServerInfo() == getServerInfo()) {
                return this.serverConnection;
            }
        }
        return this.serverConnection = this.getNewServerConnection(this.serverConnectionListener, this.serverConnectionPacketHandler);
    }

    @Override
    public TriState getPermissionValue(Permission permission, boolean allowCached) {
        if (allowCached) {
            return getServerConnection().getCachedPermissionValue(permission);
        }
        try {
            if (System.currentTimeMillis() - lastPermissionRequest < 1000) {
                return this.getServerConnection().getCachedPermissionValue(permission);
            }
            lastPermissionRequest = System.currentTimeMillis();
            return PacketCallback.request(this.getServerConnection(),
                    new PermissionCheckRequestPacket(permission), PermissionCheckResponsePacket.class)
                .thenApply(responsePacket -> {
                    responsePacket.getPermissions().forEach(getServerConnection()::setPermissionValue);
                    return responsePacket.getPermissions().getOrDefault(permission, TriState.NOT_SET);
                })
                .get(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            WegProxy.getInstance().getLogger().log(Level.SEVERE, "Failed to request permission '" + permission.getPermission() + "' for user '" + this.getName() + "' on server '" + getServer().getServerInfo().getName() + "'", e);
            return TriState.NOT_SET;
        } catch (TimeoutException e) {
            return TriState.NOT_SET;
        }
    }

    protected abstract WegServerInfo getServerInfo();

    protected abstract ServerConnection getNewServerConnection(ServerConnectionListener serverConnectionListener, AbstractPacketHandler packetHandler);

    @Override
    public @NotNull Identity identity() {
        return this.identity;
    }

    @Override
    public abstract void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type);

    @Override
    public abstract void sendActionBar(@NotNull Component message);

    @Override
    public @NotNull Pointers pointers() {
        return Pointers.builder()
            .withDynamic(Identity.UUID, this::getUniqueId)
            .withDynamic(Identity.NAME, this::getName)
            .withDynamic(Identity.DISPLAY_NAME, this::getDisplayName)
            .withDynamic(Identity.LOCALE, this::getLocale)
            .build();
    }

    private class IdentityImpl implements Identity {

        @Override
        public java.util.@NotNull UUID uuid() {
            return WegCorePlayer.this.getUniqueId();
        }
    }
}
