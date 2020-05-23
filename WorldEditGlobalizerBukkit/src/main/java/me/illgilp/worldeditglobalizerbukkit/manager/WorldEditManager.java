package me.illgilp.worldeditglobalizerbukkit.manager;

import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.illgilp.worldeditglobalizerbukkit.WorldEditGlobalizerBukkit;
import me.illgilp.worldeditglobalizerbukkit.runnables.ClipboardRunnable;
import org.bukkit.entity.Player;

public abstract class WorldEditManager {

    private Map<Player, ClipboardRunnable> clipboardRunnables = new HashMap<>();


    public abstract boolean readAndSetClipboard(Player player, byte[] data, int hashCode);

    public abstract ClipboardHolder getClipboardHolder(Player player);

    public abstract byte[] writeClipboard(ClipboardHolder clipboardHolder);

    public ClipboardRunnable getClipboardRunnable(Player player) {
        return clipboardRunnables.get(player);
    }

    protected abstract ClipboardRunnable getRunnable(Player player);

    public abstract BlockArrayClipboard createBlockArrayClipboard(Region region, UUID uuid);

    public boolean startClipboardRunnable(Player player) {
        if (!WorldEditGlobalizerBukkit.getInstance().isUsable()) {
            return false;
        }
        ClipboardRunnable clipboardRunnable = getRunnable(player);
        clipboardRunnable.runTaskTimerAsynchronously(WorldEditGlobalizerBukkit.getInstance(), 20*10, 20);
        clipboardRunnables.put(player, clipboardRunnable);
        return true;
    }

    public void cancelClipboardRunnable(Player player) {
        ClipboardRunnable clipboardRunnable = getClipboardRunnable(player);
        if (clipboardRunnable != null) {
            if (!clipboardRunnable.isCancelled()) {
                clipboardRunnable.cancel();
            }

            clipboardRunnables.remove(player);
        }
    }

    public void cancelAllClipboardRunnable() {
        new ArrayList<>(clipboardRunnables.keySet()).forEach(this::cancelClipboardRunnable);
    }

    public void setClipboardHash(Player player, int clipboardHash) {
        ClipboardRunnable clipboardRunnable = getClipboardRunnable(player);
        if (clipboardRunnable == null) {
            if (startClipboardRunnable(player)) {
                setClipboardHash(player, clipboardHash);
            }
            return;
        }
        clipboardRunnable.setClipboardHash(clipboardHash);
    }

}
