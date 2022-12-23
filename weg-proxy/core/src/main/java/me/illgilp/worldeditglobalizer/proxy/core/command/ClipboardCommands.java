package me.illgilp.worldeditglobalizer.proxy.core.command;

import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.CLIPBOARD_EMPTY;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.CLIPBOARD_EMPTY_OTHER;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_ARG_PLAYER;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_ERROR_SERVER_NOT_USABLE;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_WEG_CLIPBOARD_CLEAR_DESCRIPTION;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_WEG_CLIPBOARD_CLEAR_SUCCESS;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_WEG_CLIPBOARD_DOWNLOAD_DESCRIPTION;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_WEG_CLIPBOARD_INFO_DESCRIPTION;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_WEG_CLIPBOARD_INFO_LAYOUT;
import static me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey.COMMAND_WEG_CLIPBOARD_UPLOAD_DESCRIPTION;
import static me.illgilp.worldeditglobalizer.common.permission.Permission.COMMAND_CLIPBOARD_CLEAR;
import static me.illgilp.worldeditglobalizer.common.permission.Permission.COMMAND_CLIPBOARD_CLEAR_OTHER;
import static me.illgilp.worldeditglobalizer.common.permission.Permission.COMMAND_CLIPBOARD_DOWNLOAD;
import static me.illgilp.worldeditglobalizer.common.permission.Permission.COMMAND_CLIPBOARD_INFO;
import static me.illgilp.worldeditglobalizer.common.permission.Permission.COMMAND_CLIPBOARD_INFO_OTHER;
import static me.illgilp.worldeditglobalizer.common.permission.Permission.COMMAND_CLIPBOARD_UPLOAD;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.proxy.core.api.clipboard.WegClipboard;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.intake.annotation.Source;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegOfflinePlayer;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegPlayer;
import me.illgilp.worldeditglobalizer.proxy.core.api.server.ServerNotUsableException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.Command;
import me.illgilp.worldeditglobalizer.proxy.core.intake.Require;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.annotation.OptionalArg;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.annotation.Translated;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class ClipboardCommands {

    @Command(
        aliases = { "upload" },
        max = 0,
        desc = COMMAND_WEG_CLIPBOARD_UPLOAD_DESCRIPTION
    )
    @Require(
        value = COMMAND_CLIPBOARD_UPLOAD
    )
    public void upload(@Source WegPlayer source) {
        try {
            source.requestClipboardUpload();
        } catch (ServerNotUsableException e) {
            MessageHelper.builder()
                .translation(COMMAND_ERROR_SERVER_NOT_USABLE)
                .sendMessageTo(source);
        }
    }

    @Command(
        aliases = { "download" },
        max = 0,
        desc = COMMAND_WEG_CLIPBOARD_DOWNLOAD_DESCRIPTION
    )
    @Require(
        value = COMMAND_CLIPBOARD_DOWNLOAD
    )
    public void download(@Source WegPlayer source) {
        try {
            if (!source.downloadClipboard()) {
                MessageHelper.builder()
                    .translation(CLIPBOARD_EMPTY)
                    .sendMessageTo(source);
            }
        } catch (ServerNotUsableException e) {
            MessageHelper.builder()
                .translation(COMMAND_ERROR_SERVER_NOT_USABLE)
                .sendMessageTo(source);
        }
    }

    @Command(
        aliases = { "info" },
        max = 1,
        desc = COMMAND_WEG_CLIPBOARD_INFO_DESCRIPTION
    )
    @Require(
        value = COMMAND_CLIPBOARD_INFO
    )
    public void info(
        @Source WegPlayer source,
        @OptionalArg @Translated(COMMAND_ARG_PLAYER) @Require(COMMAND_CLIPBOARD_INFO_OTHER) WegOfflinePlayer other
    ) {

        WegOfflinePlayer player = other == null ? source : other;
        Optional<WegClipboard> clipboard = player.getClipboardContainer().getClipboard();
        if (!clipboard.isPresent()) {
            if (player.getUniqueId().equals(source.getUniqueId())) {
                MessageHelper.builder()
                    .translation(CLIPBOARD_EMPTY)
                    .sendMessageTo(source);
            } else {
                MessageHelper.builder()
                    .translation(CLIPBOARD_EMPTY_OTHER)
                    .tagResolver(Placeholder.unparsed("player_name", player.getName()))
                    .sendMessageTo(source);
            }
            return;
        }

        MessageHelper.builder()
            .translation(COMMAND_WEG_CLIPBOARD_INFO_LAYOUT)
            .tagResolver(Placeholder.unparsed("player_name", player.getName()))
            .tagResolver(Placeholder.unparsed("player_uuid", player.getUniqueId().toString()))
            .tagResolver(Formatter.date("upload_date", LocalDateTime.ofInstant(clipboard.get().getUploadDate(), ZoneId.systemDefault())))
            .tagResolver(Placeholder.unparsed("clipboard_size", BigDecimal.valueOf(clipboard.get().getSize()).toPlainString()))
            .sendMessageTo(source);
    }


    @Command(
        aliases = { "clear" },
        max = 1,
        desc = COMMAND_WEG_CLIPBOARD_CLEAR_DESCRIPTION
    )
    @Require(
        value = COMMAND_CLIPBOARD_CLEAR
    )
    public void clear(
        @Source WegPlayer source,
        @OptionalArg @Translated(COMMAND_ARG_PLAYER) @Require(COMMAND_CLIPBOARD_CLEAR_OTHER) WegOfflinePlayer other
    ) {

        WegOfflinePlayer player = other == null ? source : other;
        if (!player.getClipboardContainer().hasClipboard()) {
            if (player.getUniqueId().equals(source.getUniqueId())) {
                MessageHelper.builder()
                    .translation(CLIPBOARD_EMPTY)
                    .sendMessageTo(source);
            } else {
                MessageHelper.builder()
                    .translation(CLIPBOARD_EMPTY_OTHER)
                    .tagResolver(Placeholder.unparsed("player_name", player.getName()))
                    .sendMessageTo(source);
            }
            return;
        }
        player.getClipboardContainer().clear();
        MessageHelper.builder()
            .translation(COMMAND_WEG_CLIPBOARD_CLEAR_SUCCESS)
            .sendMessageTo(source);
    }
}
