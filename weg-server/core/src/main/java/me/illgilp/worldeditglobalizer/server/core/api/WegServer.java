package me.illgilp.worldeditglobalizer.server.core.api;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import me.illgilp.worldeditglobalizer.server.core.WegServerCore;
import me.illgilp.worldeditglobalizer.server.core.api.player.WegPlayer;
import me.illgilp.worldeditglobalizer.server.core.config.ServerConfig;
import net.kyori.adventure.audience.Audience;

public interface WegServer {

    static WegServer getInstance() {
        return WegServerCore.getInstance();
    }

    ServerConfig getServerConfig();

    Optional<WegPlayer> getPlayer(UUID uniqueId);

    Optional<WegPlayer> getPlayer(String name);

    Collection<WegPlayer> getPlayers();

    File getDataFolder();

    InputStream getResource(String path);

    Logger getLogger();

    Audience getConsoleAudience();

}
