package me.illgilp.worldeditglobalizerbungee.player;

import me.illgilp.worldeditglobalizerbungee.clipboard.Clipboard;
import me.illgilp.worldeditglobalizerbungee.manager.ClipboardManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Player{

    private ProxiedPlayer proxiedPlayer;


    private static Map<UUID,Player> players = new HashMap<>();

    private Player(ProxiedPlayer proxiedPlayer) {
        this.proxiedPlayer = proxiedPlayer;
    }

    private boolean pluginOnCurrentServerInstalled = false;

    public static Player getPlayer(ProxiedPlayer player){
        if(players.containsKey(player.getUniqueId()))return players.get(player.getUniqueId());
        Player p = new Player(player);
        players.put(player.getUniqueId(),p);
        return p;
    }

    public static Player getPlayer(String name){
        if(ProxyServer.getInstance().getPlayer(name) == null)return null;
        if(players.containsKey(ProxyServer.getInstance().getPlayer(name).getUniqueId()))return players.get(ProxyServer.getInstance().getPlayer(name).getUniqueId());
        Player p = new Player(ProxyServer.getInstance().getPlayer(name));
        players.put(p.proxiedPlayer.getUniqueId(),p);
        return p;
    }

    public static Player getPlayer(UUID uuid){
        if(ProxyServer.getInstance().getPlayer(uuid) == null)return null;
        if(players.containsKey(uuid))return players.get(uuid);
        Player p = new Player(ProxyServer.getInstance().getPlayer(uuid));
        players.put(p.proxiedPlayer.getUniqueId(),p);
        return p;
    }

    public ProxiedPlayer getProxiedPlayer() {
        return proxiedPlayer;
    }

    public Clipboard getClipboard() {
        return ClipboardManager.getInstance().getClipboard(proxiedPlayer.getUniqueId());
    }

    public void setClipboard(Clipboard clipboard) {
        ClipboardManager.getInstance().saveClipboard(clipboard);
    }

    public boolean hasClipboard(){
        return ClipboardManager.getInstance().hasClipboard(proxiedPlayer.getUniqueId());
    }

    public boolean isPluginOnCurrentServerInstalled() {
        return pluginOnCurrentServerInstalled;
    }

    public void setPluginOnCurrentServerInstalled(boolean pluginonCurrentServerInstalled) {
        this.pluginOnCurrentServerInstalled = pluginonCurrentServerInstalled;
    }
}
