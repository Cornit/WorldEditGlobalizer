package me.illgilp.worldeditglobalizerbukkit.listener;

import me.illgilp.worldeditglobalizerbukkit.WorldEditGlobalizerBukkit;
import me.illgilp.worldeditglobalizerbukkit.runnables.AWEClipboardRunnable;
import me.illgilp.worldeditglobalizerbukkit.runnables.ClipboardRunnable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (WorldEditGlobalizerBukkit.getInstance().isAsyncWorldEdit()) {
            new AWEClipboardRunnable(e.getPlayer()).runTaskTimerAsynchronously(WorldEditGlobalizerBukkit.getInstance(), 20, 20);
        } else {
            new ClipboardRunnable(e.getPlayer()).runTaskTimerAsynchronously(WorldEditGlobalizerBukkit.getInstance(), 20, 20);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (WorldEditGlobalizerBukkit.getInstance().isAsyncWorldEdit()) {
            AWEClipboardRunnable.stop(e.getPlayer().getName());
        } else {
            ClipboardRunnable.stop(e.getPlayer().getName());
        }
    }

}
