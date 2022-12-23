package me.illgilp.worldeditglobalizer.server.bukkit.worldedit.adapter.v1_8_fawe_22_3_9.schematic;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.registry.LegacyWorldData;

public class WegClipboardHolder extends ClipboardHolder {

    private final int hashCode;

    public WegClipboardHolder(Clipboard clipboard, int hashcode) {
        super(clipboard, LegacyWorldData.getInstance());
        this.hashCode = hashcode;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
