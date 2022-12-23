package me.illgilp.worldeditglobalizer.server.bukkit.api.worldedit.adapter;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.illgilp.worldeditglobalizer.server.core.api.clipboard.WegClipboard;
import me.illgilp.worldeditglobalizer.server.core.api.player.WegPlayer;

@RequiredArgsConstructor
public abstract class WorldEditAdapter {

    private final WorldEditAdapterFilter filter;

    public WorldEditAdapterFilter getFilter() {
        return filter;
    }

    public abstract Optional<WegClipboard> getClipboardOfPlayer(WegPlayer player);

    public abstract boolean readAndSetClipboard(WegPlayer player, byte[] data, int hashCode);

    public abstract void clearClipboard(WegPlayer player);
}
