package me.illgilp.worldeditglobalizer.proxy.core.server;

import java.util.Collection;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegPlayer;
import me.illgilp.worldeditglobalizer.proxy.core.api.server.WegServerInfo;

public abstract class WegCoreServerInfo implements WegServerInfo {


    public abstract String getName();

    public abstract Collection<WegPlayer> getPlayers();

}
