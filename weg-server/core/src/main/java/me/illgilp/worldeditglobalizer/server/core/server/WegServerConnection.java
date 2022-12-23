package me.illgilp.worldeditglobalizer.server.core.server;


import me.illgilp.worldeditglobalizer.common.network.Connection;

public interface WegServerConnection extends Connection {

    State getState();

    enum State {
        UNKNOWN,
        INCOMPATIBLE,
        WRONG_CONFIGURATION,
        USABLE
    }

}
