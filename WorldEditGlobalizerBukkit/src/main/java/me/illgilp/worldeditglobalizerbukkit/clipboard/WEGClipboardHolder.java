package me.illgilp.worldeditglobalizerbukkit.clipboard;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;

public class WEGClipboardHolder extends ClipboardHolder {

    private int hashCode;

    public WEGClipboardHolder(Clipboard clipboard, int hashcode) {
        super(clipboard);
        this.hashCode = hashcode;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
