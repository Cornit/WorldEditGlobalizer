package me.illgilp.worldeditglobalizerbukkit.listener;

import me.illgilp.worldeditglobalizerbukkit.manager.VersionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        VersionManager.getInstance().getWorldEditManager().startClipboardRunnable(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        VersionManager.getInstance().getWorldEditManager().cancelClipboardRunnable(e.getPlayer());
    }

}
