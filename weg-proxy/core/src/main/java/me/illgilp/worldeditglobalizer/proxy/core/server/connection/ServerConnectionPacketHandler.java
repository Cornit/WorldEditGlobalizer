package me.illgilp.worldeditglobalizer.proxy.core.server.connection;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.logging.Level;
import lombok.RequiredArgsConstructor;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;
import me.illgilp.worldeditglobalizer.common.network.AbstractPacketHandler;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ActionBarPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ClipboardDataPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.KeepAlivePacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.MessagePacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.PermissionCheckResponsePacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ProxyConfigRequestPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ProxyConfigResponsePacket;
import me.illgilp.worldeditglobalizer.proxy.core.api.WegProxy;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegPlayer;
import me.illgilp.worldeditglobalizer.proxy.core.player.WegCorePlayer;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

@RequiredArgsConstructor
public class ServerConnectionPacketHandler extends AbstractPacketHandler {

    private final WegCorePlayer player;

    @Override
    public void handle(KeepAlivePacket packet) {
        if (packet.isRequest()) {
            player.getServer().sendPacket(new KeepAlivePacket(packet.getId(), false));
        }
    }

    @Override
    public void handle(PermissionCheckResponsePacket packet) {
        packet.getPermissions().forEach(player.getServerConnection()::setPermissionValue);
    }

    @Override
    public void handle(ProxyConfigRequestPacket packet) {
        if (packet.isRequest()) {
            ProxyConfigResponsePacket responsePacket = new ProxyConfigResponsePacket(WegProxy.getInstance().getProxyConfig());
            responsePacket.setId(packet.getId());
            responsePacket.setRequest(false);
            player.getServer().sendPacket(responsePacket);
        }
    }

    @Override
    public void handle(ClipboardDataPacket packet) {
        try {
            player.getClipboardContainer().setClipboard(packet.getHash(), packet.getClipboardData());
            MessageHelper.builder()
                .translation(TranslationKey.CLIPBOARD_UPLOAD_DONE)
                .tagResolver(Placeholder.unparsed("clipboard_size", BigDecimal.valueOf(packet.getClipboardData().length).toPlainString()))
                .sendMessageTo(player);
        } catch (IOException e) {
            MessageHelper.builder()
                .translation(TranslationKey.CLIPBOARD_UPLOAD_ERROR)
                .sendMessageTo(player);
            WegProxy.getInstance().getLogger().log(Level.SEVERE, "Exception while uploading clipboard of player '" + player.getName() + "'", e);
        }
    }

    @Override
    public void handle(MessagePacket packet) {
        player.sendMessage(
            WegProxy.getInstance().getPlayer(packet.getSource())
                .map(WegPlayer::identity)
                .orElse(Identity.nil()),
            packet.getMessage(),
            packet.getMessageType()
        );
    }

    @Override
    public void handle(ActionBarPacket packet) {
        player.sendActionBar(packet.getMessage());
    }
}
