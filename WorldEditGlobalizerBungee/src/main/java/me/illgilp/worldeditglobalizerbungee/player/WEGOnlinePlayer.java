package me.illgilp.worldeditglobalizerbungee.player;

import java.util.Collection;
import java.util.UUID;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.clipboard.Clipboard;
import me.illgilp.worldeditglobalizerbungee.events.ServerUsabilityChangedEvent;
import me.illgilp.worldeditglobalizerbungee.manager.ClipboardManager;
import me.illgilp.worldeditglobalizerbungee.manager.MessageManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class WEGOnlinePlayer implements Player {

    private final ProxiedPlayer proxiedPlayer;
    private ServerUsability serverUsability;

    private String serverVersion;

    public WEGOnlinePlayer(ProxiedPlayer proxiedPlayer) {
        this.proxiedPlayer = proxiedPlayer;
    }

    @Override
    public void setServerUsability(ServerUsability serverUsability) {
        ServerUsabilityChangedEvent event = null;
        if (this.serverUsability != serverUsability) {
            event = new ServerUsabilityChangedEvent(this, this.serverUsability, serverUsability);
        }
        this.serverUsability = serverUsability;
        if (event != null) {
            BungeeCord.getInstance().getPluginManager().callEvent(event);
        }
    }

    @Override
    public ServerUsability getServerUsability() {
        return serverUsability;
    }

    @Override
    public UUID getUniqueId() {
        return this.proxiedPlayer.getUniqueId();
    }

    @Override
    public String getName() {
        return this.proxiedPlayer.getName();
    }

    @Override
    @Deprecated
    public void sendMessage(String s) {
        this.proxiedPlayer.sendMessage(s);
    }

    @Override
    @Deprecated
    public void sendMessages(String... strings) {
        this.proxiedPlayer.sendMessages(strings);
    }

    @Override
    public void sendMessage(BaseComponent... baseComponents) {
        this.proxiedPlayer.sendMessage(baseComponents);
    }

    @Override
    public void sendMessage(BaseComponent baseComponent) {
        this.proxiedPlayer.sendMessage(baseComponent);
    }

    @Override
    public Collection<String> getGroups() {
        return this.proxiedPlayer.getGroups();
    }

    @Override
    public void addGroups(String... strings) {
        this.proxiedPlayer.addGroups(strings);
    }

    @Override
    public void removeGroups(String... strings) {
        this.proxiedPlayer.removeGroups(strings);
    }

    @Override
    public boolean hasPermission(String s) {
        return this.proxiedPlayer.hasPermission(s);
    }

    @Override
    public void setPermission(String s, boolean b) {
        this.proxiedPlayer.setPermission(s, b);
    }

    @Override
    public Collection<String> getPermissions() {
        return this.proxiedPlayer.getPermissions();
    }

    @Override
    public boolean isOnline() {
        return this.proxiedPlayer.isConnected();
    }

    @Override
    public ProxiedPlayer getProxiedPlayer() {
        return this.proxiedPlayer;
    }

    @Override
    public boolean hasClipboard() {
        return ClipboardManager.getInstance().hasClipboard(this.proxiedPlayer.getUniqueId());
    }

    @Override
    public void setClipboard(Clipboard clipboard) {
        if (clipboard != null) {
            ClipboardManager.getInstance().saveClipboard(clipboard);
        }
    }

    @Override
    public boolean removeClipboard() {
        return ClipboardManager.getInstance().removeClipboard(this.proxiedPlayer.getUniqueId());
    }

    @Override
    public Clipboard getClipboard() {
        return ClipboardManager.getInstance().getClipboard(this.proxiedPlayer.getUniqueId());
    }

    @Override
    public boolean isExisting() {
        return true;
    }

    @Override
    public boolean sendIncompatibleMessage(ServerUsability serverUsability) {
        switch (serverUsability) {
            case PLUGIN_NOT_INSTALLED:
                MessageManager.sendMessage(this, "command.server.cannotUse.pluginNotInstalled");
                break;
            case INCOMPATIBLE_VERSION:
                MessageManager.sendMessage(
                    this,
                    "command.server.cannotUse.incompatibleVersion",
                    WorldEditGlobalizerBungee.getInstance().getDescription().getVersion(),
                    proxiedPlayer.getServer().getInfo().getName(), serverVersion);
                break;
            case KEY_NOT_SET:
                MessageManager.sendMessage(this, "command.server.cannotUse.secretKeyNotSet");
                break;
            case KEY_NOT_CORRECT:
                MessageManager.sendMessage(this, "command.server.cannotUse.incorrectSecretKey");
                break;
        }
        return serverUsability != ServerUsability.UNKNOWN && serverUsability != ServerUsability.KEY_CORRECT;
    }

    @Override
    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    @Override
    public String getServerVersion() {
        return serverVersion;
    }

    @Override
    public String toString() {
        return "WEGOnlinePlayer{" +
            "name='" + getName() + '\'' +
            ", existing=" + isExisting() +
            '}';
    }
}
