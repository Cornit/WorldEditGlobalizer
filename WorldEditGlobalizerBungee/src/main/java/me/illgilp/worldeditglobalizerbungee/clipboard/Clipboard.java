package me.illgilp.worldeditglobalizerbungee.clipboard;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public final class Clipboard {

    private UUID owner;
    private byte[] data;
    private int hash;
    private String fromServer;

    public Clipboard(UUID owner, byte[] data, int hash, String fromServer) {
        this.owner = owner;
        this.data = data;
        this.hash = hash;
        this.fromServer = fromServer;
    }

    public UUID getOwner() {
        return owner;
    }


    public byte[] getData() {
        return data;
    }

    public int getHash() {
        return hash;
    }

    public String getFromServer() {
        return fromServer;
    }
}
