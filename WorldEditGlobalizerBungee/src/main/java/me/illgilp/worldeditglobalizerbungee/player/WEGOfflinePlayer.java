package me.illgilp.worldeditglobalizerbungee.player;

import java.util.UUID;
import me.illgilp.worldeditglobalizerbungee.clipboard.Clipboard;
import me.illgilp.worldeditglobalizerbungee.manager.ClipboardManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class WEGOfflinePlayer implements OfflinePlayer {

    private UUID uniqueId;
    private String name;
    private boolean existing;

    public WEGOfflinePlayer(UUID uniqueId, String name) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.existing = true;
    }

    public WEGOfflinePlayer(UUID uniqueId, String name, boolean existing) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.existing = existing;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isOnline() {
        return BungeeCord.getInstance().getPlayer(this.uniqueId) != null;
    }

    @Override
    public ProxiedPlayer getProxiedPlayer() {
        return BungeeCord.getInstance().getPlayer(this.uniqueId);
    }

    @Override
    public boolean hasClipboard() {
        return ClipboardManager.getInstance().hasClipboard(this.uniqueId);
    }

    @Override
    public void setClipboard(Clipboard clipboard) {
        if (clipboard != null) {
            ClipboardManager.getInstance().saveClipboard(clipboard);
        }
    }

    @Override
    public boolean removeClipboard() {
        return ClipboardManager.getInstance().removeClipboard(this.uniqueId);
    }

    @Override
    public Clipboard getClipboard() {
        return ClipboardManager.getInstance().getClipboard(this.uniqueId);
    }

    @Override
    public boolean isExisting() {
        return this.existing;
    }

    @Override
    public String toString() {
        return "WEGOfflinePlayer{" +
            "name='" + name + '\'' +
            ", existing=" + existing +
            '}';
    }
}
