package me.illgilp.worldeditglobalizer.proxy.core.api.server;

public class ServerNotUsableException extends Exception {

    private final WegServer server;

    public ServerNotUsableException(WegServer server) {
        this.server = server;
    }
}
