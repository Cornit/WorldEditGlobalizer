package me.illgilp.worldeditglobalizer.proxy.core.api.server;

import me.illgilp.worldeditglobalizer.common.network.Connection;

public interface WegServer extends Connection {

    WegServerInfo getServerInfo();

    State getState();

    boolean isUsable();

    enum State {
        UNKNOWN,
        INCOMPATIBLE,
        WRONG_CONFIGURATION,
        USABLE
    }

}
