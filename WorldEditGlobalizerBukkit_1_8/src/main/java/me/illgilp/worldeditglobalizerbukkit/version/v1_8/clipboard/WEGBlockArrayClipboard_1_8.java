package me.illgilp.worldeditglobalizerbukkit.version.v1_8.clipboard;

import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.Region;
import me.illgilp.worldeditglobalizerbukkit.clipboard.WEGBlockArrayClipboard;

public class WEGBlockArrayClipboard_1_8 extends BlockArrayClipboard implements Clipboard, WEGBlockArrayClipboard {
    private int hashCode;

    public WEGBlockArrayClipboard_1_8(Region region) {
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
