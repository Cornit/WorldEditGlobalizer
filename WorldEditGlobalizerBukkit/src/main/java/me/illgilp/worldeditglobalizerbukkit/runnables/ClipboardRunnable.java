package me.illgilp.worldeditglobalizerbukkit.runnables;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class ClipboardRunnable extends BukkitRunnable {

    private boolean cancelled;

    public abstract void setClipboardHash(int clipboardHash);

    public abstract Player getPlayer();

    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        cancelled = true;
        super.cancel();
    }
}
