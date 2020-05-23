package me.illgilp.worldeditglobalizerbukkit.version.v1_13.clipboard;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;

public class WEGClipboardHolder_1_13 extends ClipboardHolder {

    private int hashCode;

    public WEGClipboardHolder_1_13(Clipboard clipboard, int hashcode) {
        super(clipboard);
        this.hashCode = hashcode;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
