package me.illgilp.worldeditglobalizerbungee.events;

import me.illgilp.worldeditglobalizerbungee.player.Player;
import me.illgilp.worldeditglobalizerbungee.player.ServerUsability;
import net.md_5.bungee.api.plugin.Event;

public class ServerUsabilityChangedEvent extends Event {

    private Player player;
    private ServerUsability from;
    private ServerUsability to;

    public ServerUsabilityChangedEvent(Player player, ServerUsability from, ServerUsability to) {
        this.player = player;
        this.from = from;
        this.to = to;
    }

    public Player getPlayer() {
        return player;
    }

    public ServerUsability getFrom() {
        return from;
    }

    public ServerUsability getTo() {
        return to;
    }
}
