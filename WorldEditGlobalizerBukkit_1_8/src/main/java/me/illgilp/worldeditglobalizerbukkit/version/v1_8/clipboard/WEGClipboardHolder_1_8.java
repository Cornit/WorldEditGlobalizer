package me.illgilp.worldeditglobalizerbukkit.version.v1_8.clipboard;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.registry.LegacyWorldData;

public class WEGClipboardHolder_1_8 extends ClipboardHolder {

    private int hashCode;

    public WEGClipboardHolder_1_8(Clipboard clipboard, int hashcode) {
        super(clipboard, LegacyWorldData.getInstance());
        this.hashCode = hashcode;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
