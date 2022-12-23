package me.illgilp.worldeditglobalizer.proxy.core.api;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import me.illgilp.worldeditglobalizer.proxy.core.WegProxyCore;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegOfflinePlayer;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegPlayer;
import me.illgilp.worldeditglobalizer.proxy.core.api.server.WegServerInfo;
import me.illgilp.worldeditglobalizer.proxy.core.config.ProxyConfig;

public interface WegProxy {

    static WegProxy getInstance() {
        return WegProxyCore.getInstance();
    }

    ProxyConfig getProxyConfig();

    Optional<WegPlayer> getPlayer(UUID uniqueId);

    Optional<WegPlayer> getPlayer(String name);

    Collection<WegPlayer> getPlayers();

    Optional<WegOfflinePlayer> getOfflinePlayer(UUID uniqueId);

    Optional<WegOfflinePlayer> getOfflinePlayer(String name);

    List<WegOfflinePlayer> getOfflinePlayersByNameStartingWith(String prefix);

    Optional<WegServerInfo> getServerInfo(String name);

    Collection<WegServerInfo> getServers();

    File getDataFolder();

    InputStream getResource(String path);

    Logger getLogger();

}
