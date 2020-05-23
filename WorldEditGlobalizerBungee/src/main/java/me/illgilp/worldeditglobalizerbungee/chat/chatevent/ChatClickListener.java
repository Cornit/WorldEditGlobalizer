package me.illgilp.worldeditglobalizerbungee.chat.chatevent;

import me.illgilp.worldeditglobalizerbungee.manager.ChatEventManager;
import me.illgilp.worldeditglobalizerbungee.player.Player;

public abstract class ChatClickListener {
    private String id;

    public ChatClickListener() {
        this.id = ChatEventManager.getInstance().getNextListenerId();
    }

    public abstract void onClick(Player player);

    public String getId() {
        return id;
    }
}
