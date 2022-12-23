package me.illgilp.worldeditglobalizer.server.bukkit.worldedit.adapter.v1_8_worldedit_6_1_5.schematic;


import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.Region;

public class WegBlockArrayClipboard extends BlockArrayClipboard implements Clipboard {
    private int hashCode;

    public WegBlockArrayClipboard(Region region) {
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
