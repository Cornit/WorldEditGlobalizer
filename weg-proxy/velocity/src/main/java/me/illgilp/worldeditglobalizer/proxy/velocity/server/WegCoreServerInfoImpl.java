package me.illgilp.worldeditglobalizer.proxy.velocity.server;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.illgilp.worldeditglobalizer.proxy.core.api.WegProxy;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegPlayer;
import me.illgilp.worldeditglobalizer.proxy.core.server.WegCoreServerInfo;


@RequiredArgsConstructor
public class WegCoreServerInfoImpl extends WegCoreServerInfo {

    private final RegisteredServer registeredServer;

    @Override
    public String getName() {
        return this.registeredServer.getServerInfo().getName();
    }

    @Override
    public Collection<WegPlayer> getPlayers() {
        return this.registeredServer.getPlayersConnected().stream()
            .map(Player::getUniqueId)
            .map(WegProxy.getInstance()::getPlayer)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
    }
}
