package me.illgilp.worldeditglobalizer.server.core.player;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import lombok.Getter;
import lombok.Setter;
import me.illgilp.worldeditglobalizer.common.config.CommonProxyConfig;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.messages.tag.ProgressBarTag;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;
import me.illgilp.worldeditglobalizer.common.network.PacketCallback;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ActionBarPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ClipboardDataPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.MessagePacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ProxyConfigRequestPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ProxyConfigResponsePacket;
import me.illgilp.worldeditglobalizer.server.core.api.WegServer;
import me.illgilp.worldeditglobalizer.server.core.api.clipboard.WegClipboard;
import me.illgilp.worldeditglobalizer.server.core.api.player.WegPlayer;
import me.illgilp.worldeditglobalizer.server.core.server.WegServerConnection;
import me.illgilp.worldeditglobalizer.server.core.server.connection.ServerConnection;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

public abstract class WegCorePlayer implements WegPlayer {

    private final Identity identity = new IdentityImpl();

    private CommonProxyConfig proxyConfig;

    @Setter
    private boolean autoUploadReady = false;

    @Getter
    private ServerConnection serverConnection;

    @Override
    public WegServerConnection getConnection() {
        return serverConnection == null ? (serverConnection = this.getNewConnection()) : serverConnection;
    }

    protected abstract ServerConnection getNewConnection();

    public abstract Component getDisplayName();

    public abstract Locale getLocale();

    @Override
    public @NotNull Identity identity() {
        return this.identity;
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
        if (!WegServer.getInstance().getServerConfig().isUseFallbackMessageSending()) {
            sendMessageInternal(source, message, type);
        } else {
            getConnection().sendPacket(new MessagePacket(source.uuid(), message, type));
        }
    }

    protected abstract void sendMessageInternal(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type);

    @Override
    public void sendActionBar(@NotNull Component message) {
        if (!WegServer.getInstance().getServerConfig().isUseFallbackMessageSending()) {
            sendActionBarInternal(message);
        } else {
            getConnection().sendPacket(new ActionBarPacket(message));
        }
    }


    public abstract void sendActionBarInternal(@NotNull Component message);


    @Override
    public boolean uploadClipboard() {
        final Optional<WegClipboard> optClipboard = getClipboard();
        if (!optClipboard.isPresent()) {
            MessageHelper.builder()
                .translation(TranslationKey.CLIPBOARD_EMPTY)
                .sendMessageTo(this);
            return false;
        }
        MessageHelper.builder()
            .translation(TranslationKey.CLIPBOARD_UPLOAD_PREPARE)
            .sendMessageTo(this);
        WegClipboard clipboard = optClipboard.get();
        byte[] clipboardData;
        try {
            clipboardData = clipboard.write();
        } catch (IOException e) {
            MessageHelper.builder()
                .translation(TranslationKey.CLIPBOARD_UPLOAD_ERROR)
                .sendMessageTo(this);
            WegServer.getInstance().getLogger()
                .log(Level.SEVERE, "Exception while writing clipboard of player '" + this.getName() + "'", e);
            return false;
        }
        if (clipboardData.length > this.getProxyConfig().getMaxClipboardSize()) {
            MessageHelper.builder()
                .translation(TranslationKey.CLIPBOARD_UPLOAD_TOO_LARGE)
                .sendMessageTo(this);
            return false;
        }
        MessageHelper.builder()
            .translation(TranslationKey.CLIPBOARD_UPLOAD_START)
            .tagResolver(Placeholder.unparsed("clipboard_size", BigDecimal.valueOf(clipboardData.length).toPlainString()))
            .sendMessageTo(this);
        this.getConnection().sendPacket(
            new ClipboardDataPacket(clipboard.getHash(), clipboardData),
            percentage -> {
                String percFormatted = new DecimalFormat("000.0").format(percentage * 100.0);
                MessageHelper.builder()
                    .translation(TranslationKey.CLIPBOARD_UPLOAD_PROGRESS)
                    .tagResolver(TagResolver.resolver("progressbar", (argumentQueue, context) ->
                        ProgressBarTag.create(percentage, argumentQueue, context)))
                    .tagResolver(Placeholder.unparsed("percentage", percFormatted))
                    .sendActionBarTo(this);
            }
        );
        return true;
    }

    @Override
    public CommonProxyConfig getProxyConfig() {
        if (proxyConfig != null) {
            return proxyConfig;
        }
        try {
            proxyConfig = PacketCallback.request(getConnection(), new ProxyConfigRequestPacket(), ProxyConfigResponsePacket.class)
                .get(500, TimeUnit.MILLISECONDS)
                .getConfig();
            return proxyConfig;
        } catch (InterruptedException | ExecutionException e) {
            WegServer.getInstance().getLogger().log(Level.SEVERE, "exception while requesting proxy config", e);
        } catch (TimeoutException ignored) {
        }
        return new CommonProxyConfig();
    }

    public void setProxyConfig(CommonProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

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
