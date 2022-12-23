package me.illgilp.worldeditglobalizer.proxy.core.api.server;

import java.util.Collection;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegPlayer;

public interface WegServerInfo {

    String getName();

    Collection<WegPlayer> getPlayers();

}
