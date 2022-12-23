package me.illgilp.worldeditglobalizer.proxy.core.command;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.ProxyConfigResponsePacket;
import me.illgilp.worldeditglobalizer.common.permission.Permission;
import me.illgilp.worldeditglobalizer.proxy.core.api.WegProxy;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.CommandSource;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.intake.annotation.Source;
import me.illgilp.worldeditglobalizer.proxy.core.api.server.WegServer;
import me.illgilp.worldeditglobalizer.proxy.core.intake.Command;
import me.illgilp.worldeditglobalizer.proxy.core.intake.Require;

public class WegCommands {

    @Command(
        aliases = { "reload" },
        min = 0,
        max = 1,
        desc = TranslationKey.COMMAND_WEG_RELOAD_DESCRIPTION
    )
    @Require(
        value = Permission.COMMAND_RELOAD
    )
    public void reload(@Source CommandSource source) {
        MessageHelper.builder()
            .translation(TranslationKey.COMMAND_WEG_RELOAD_START)
            .sendMessageTo(source);
        try {
            WegProxy.getInstance().getProxyConfig().load();
        } catch (IOException e) {
            WegProxy.getInstance().getLogger().log(Level.SEVERE, "Failed to reload config", e);
            MessageHelper.builder()
                .translation(TranslationKey.COMMAND_WEG_RELOAD_FAILED)
                .sendMessageTo(source);
            return;
        }
        WegProxy.getInstance().getPlayers().stream()
            .filter(player -> player.getServer().getState() == WegServer.State.USABLE)
            .forEach(player -> {
                player.getServer().sendPacket(new ProxyConfigResponsePacket(
                    new UUID(0, 0),
                    false,
                    WegProxy.getInstance().getProxyConfig()
                ));
            });
        MessageHelper.builder()
            .translation(TranslationKey.COMMAND_WEG_RELOAD_FINISH)
            .sendMessageTo(source);
    }
}
