package me.illgilp.worldeditglobalizer.server.bukkit.worldedit.adapter.v1_8_fawe_22_3_9.schematic;


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
