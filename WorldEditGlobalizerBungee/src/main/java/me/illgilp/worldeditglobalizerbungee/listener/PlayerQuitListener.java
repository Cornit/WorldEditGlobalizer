package me.illgilp.worldeditglobalizerbungee.listener;

import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.manager.ClipboardManager;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerDisconnectEvent e) {
        if (WorldEditGlobalizerBungee.getInstance().getMainConfig().isKeepClipboard()) return;
        ClipboardManager.getInstance().removeClipboard(e.getPlayer().getUniqueId());
    }

}
