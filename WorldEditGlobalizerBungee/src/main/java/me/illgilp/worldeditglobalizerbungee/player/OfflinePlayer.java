package me.illgilp.worldeditglobalizerbungee.player;

import java.util.UUID;
import me.illgilp.worldeditglobalizerbungee.clipboard.Clipboard;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public interface OfflinePlayer {

    UUID getUniqueId();

    String getName();

    boolean isOnline();

    ProxiedPlayer getProxiedPlayer();

    boolean hasClipboard();
    void setClipboard(Clipboard clipboard);
    boolean removeClipboard();
    Clipboard getClipboard();

    boolean isExisting();

}
