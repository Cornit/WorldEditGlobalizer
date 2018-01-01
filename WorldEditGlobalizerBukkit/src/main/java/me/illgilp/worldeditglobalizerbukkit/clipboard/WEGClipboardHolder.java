package me.illgilp.worldeditglobalizerbukkit.clipboard;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.registry.LegacyWorldData;
import com.sk89q.worldedit.world.registry.WorldData;

public class WEGClipboardHolder extends ClipboardHolder {

    private int hashCode;

    public WEGClipboardHolder(Clipboard clipboard, int hashcode) {
        super(clipboard, LegacyWorldData.getInstance());
        this.hashCode = hashcode;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
