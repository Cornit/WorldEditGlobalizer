package me.illgilp.worldeditglobalizer.proxy.bungeecord.server;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.illgilp.worldeditglobalizer.proxy.core.api.WegProxy;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegPlayer;
import me.illgilp.worldeditglobalizer.proxy.core.server.WegCoreServerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;


@RequiredArgsConstructor
public class WegCoreServerInfoImpl extends WegCoreServerInfo {

    private final ServerInfo serverInfo;

    @Override
    public String getName() {
        return this.serverInfo.getName();
    }

    @Override
    public Collection<WegPlayer> getPlayers() {
        return this.serverInfo.getPlayers().stream()
            .map(ProxiedPlayer::getUniqueId)
            .map(WegProxy.getInstance()::getPlayer)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
    }
}
