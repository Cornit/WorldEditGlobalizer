package me.illgilp.worldeditglobalizerbukkit.clipboard;

import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.Region;

public class WEGBlockArrayClipboard extends BlockArrayClipboard implements Clipboard {
    public WEGBlockArrayClipboard(Region region) {
        super(region);
    }

    private int hashCode;

    public void setHashCode(int hashCode) {
        this.hashCode = hashCode;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
