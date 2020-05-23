package me.illgilp.worldeditglobalizerbungee.manager;

import java.util.UUID;
import me.illgilp.worldeditglobalizerbungee.chat.chatevent.ChatClickListener;
import me.illgilp.worldeditglobalizerbungee.player.Player;
import me.illgilp.worldeditglobalizerbungee.util.Cache;

public class ChatEventManager {

    private static ChatEventManager instance;

    public static ChatEventManager getInstance() {
        return instance == null ? instance = new ChatEventManager() : instance;
    }

    private Cache<String, ChatClickListener> chatClickListenerCache = new Cache<>();

    public ChatEventManager() {
    }

    public String getNextListenerId() {
        String key = UUID.randomUUID().toString().replace("-","");
        while (chatClickListenerCache.containsKey(key)) {
            key = UUID.randomUUID().toString().replace("-","");
        }

        return key;
    }

    public void registerListener(ChatClickListener chatClickListener) {
        chatClickListenerCache.put(chatClickListener.getId(), chatClickListener);
    }

    public void callListener(Player player, String id) {
        ChatClickListener chatClickListener = chatClickListenerCache.get(id);
        if (chatClickListener == null) {
            MessageManager.sendMessage(player, "chat.function.notAvailable");
            return;
        } else {
            try {
                chatClickListener.onClick(player);
            } catch (Exception e) {
                e.printStackTrace();
                MessageManager.sendMessage(player, "chat.function.error");
            }
        }
    }

    public ChatClickListener getListener(String id) {
        return chatClickListenerCache.get(id);
    }
}
