package me.illgilp.worldeditglobalizer.server.core.server.connection;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;
import me.illgilp.worldeditglobalizer.common.network.AbstractPacketHandler;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ClipboardDataPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ClipboardRequestPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.KeepAlivePacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.PermissionCheckRequestPacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.PermissionCheckResponsePacket;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ProxyConfigResponsePacket;
import me.illgilp.worldeditglobalizer.common.permission.Permission;
import me.illgilp.worldeditglobalizer.common.scheduler.WegScheduler;
import me.illgilp.worldeditglobalizer.server.core.player.WegCorePlayer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

@RequiredArgsConstructor
public class ServerConnectionPacketHandler extends AbstractPacketHandler {

    private final WegCorePlayer player;

    @Override
    public void handle(KeepAlivePacket packet) {
        if (packet.isRequest()) {
            player.getConnection().sendPacket(new KeepAlivePacket(packet.getId(), false));
        }
    }

    @Override
    public void handle(ClipboardDataPacket packet) {
        WegScheduler.getInstance().getSyncExecutor().execute(() -> {
            if (player.setClipboard(packet.getClipboardData(), packet.getHash())) {
                MessageHelper.builder()
                    .translation(TranslationKey.CLIPBOARD_DOWNLOAD_DONE)
                    .tagResolver(Placeholder.unparsed("clipboard_size", BigDecimal.valueOf(packet.getClipboardData().length).toPlainString()))
                    .sendMessageTo(player);
            }
        });
    }

    @Override
    public void handle(ProxyConfigResponsePacket packet) {
        if (!packet.isRequest()) {
            if (packet.getId().equals(new UUID(0, 0))) {
                player.setProxyConfig(packet.getConfig());
            }
        }
    }

    @Override
    public void handle(PermissionCheckRequestPacket packet) {
        player.getConnection().sendPacket(new PermissionCheckResponsePacket(
            packet.getId(),
            false,
            Arrays.stream(Permission.values()).collect(Collectors.toMap(Function.identity(), player::getPermissionValue))
        ));
    }

    @Override
    public void handle(ClipboardRequestPacket packet) {
        WegScheduler.getInstance().getSyncExecutor().execute(player::uploadClipboard);
    }
}
