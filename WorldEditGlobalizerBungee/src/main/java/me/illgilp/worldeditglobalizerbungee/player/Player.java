package me.illgilp.worldeditglobalizerbungee.player;

import net.md_5.bungee.api.CommandSender;

public interface Player extends OfflinePlayer, CommandSender {

    ServerUsability getServerUsability();
    void setServerUsability(ServerUsability serverUsability);

    boolean sendIncompatibleMessage(ServerUsability serverUsability);

    void setServerVersion(String version);
    String getServerVersion();




}
