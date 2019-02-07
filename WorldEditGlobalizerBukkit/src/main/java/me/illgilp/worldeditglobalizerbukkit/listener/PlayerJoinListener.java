package me.illgilp.worldeditglobalizerbukkit.listener;

import me.illgilp.worldeditglobalizerbukkit.WorldEditGlobalizerBukkit;
import me.illgilp.worldeditglobalizerbukkit.runnables.ClipboardRunnable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        new ClipboardRunnable(e.getPlayer()).runTaskTimerAsynchronously(WorldEditGlobalizerBukkit.getInstance(), 20 * 10, 20);
    }

}
