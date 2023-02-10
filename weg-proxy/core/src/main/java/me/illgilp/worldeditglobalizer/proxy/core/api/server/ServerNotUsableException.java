package me.illgilp.worldeditglobalizer.proxy.core.api.server;

import lombok.Getter;

public class ServerNotUsableException extends Exception {

    @Getter
    private final WegServer server;

    public ServerNotUsableException(WegServer server) {
        this.server = server;
    }
}
