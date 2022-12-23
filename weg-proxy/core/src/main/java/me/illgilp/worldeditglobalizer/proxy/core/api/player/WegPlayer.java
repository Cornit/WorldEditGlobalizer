package me.illgilp.worldeditglobalizer.proxy.core.api.player;

import java.util.Locale;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.CommandSource;
import me.illgilp.worldeditglobalizer.proxy.core.api.server.ServerNotUsableException;
import me.illgilp.worldeditglobalizer.proxy.core.api.server.WegServer;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.text.Component;

public interface WegPlayer extends WegOfflinePlayer, CommandSource, Identified {

    Component getDisplayName();

    WegServer getServer();

    Locale getLocale();

    void requestClipboardUpload() throws ServerNotUsableException;

    boolean downloadClipboard() throws ServerNotUsableException;

}
