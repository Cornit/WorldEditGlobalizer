package me.illgilp.worldeditglobalizerbukkit.version.v1_13.clipboard;

import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.Region;
import me.illgilp.worldeditglobalizerbukkit.clipboard.WEGBlockArrayClipboard;

public class WEGBlockArrayClipboard_1_13 extends BlockArrayClipboard implements Clipboard, WEGBlockArrayClipboard {
    private int hashCode;

    public WEGBlockArrayClipboard_1_13(Region region) {
        super(region);
    }

    public void setHashCode(int hashCode) {
        this.hashCode = hashCode;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
