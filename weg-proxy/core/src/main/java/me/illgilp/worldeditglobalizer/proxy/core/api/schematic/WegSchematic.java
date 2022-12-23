package me.illgilp.worldeditglobalizer.proxy.core.api.schematic;

import java.io.File;
import java.io.IOException;
import me.illgilp.worldeditglobalizer.proxy.core.api.clipboard.WegClipboard;

public interface WegSchematic {

    String getName();

    File getFile();

    WegClipboard asClipboard() throws IOException;

}
