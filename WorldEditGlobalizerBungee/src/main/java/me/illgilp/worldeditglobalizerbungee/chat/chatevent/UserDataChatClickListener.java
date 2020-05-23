package me.illgilp.worldeditglobalizerbungee.chat.chatevent;

import me.illgilp.worldeditglobalizerbungee.player.Player;

public abstract class UserDataChatClickListener<T> extends ChatClickListener {

    private T userData;

    public UserDataChatClickListener() {
    }

    public UserDataChatClickListener(T userData) {
        this.userData = userData;
    }

    @Override
    public void onClick(Player player) {
        onClick(player, userData);
    }

    public abstract void onClick(Player player, T t);

    public T getUserData() {
        return userData;
    }

    public void setUserData(T t) {
        this.userData = t;
    }

    public boolean hasUserData() {
        return userData != null;
    }
}
