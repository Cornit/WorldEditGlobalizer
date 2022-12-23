package me.illgilp.worldeditglobalizer.proxy.core.api.schematic;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import me.illgilp.worldeditglobalizer.proxy.core.api.clipboard.WegClipboard;

public interface WegSchematicContainer {

    List<WegSchematic> getSchematics() throws IOException;

    Optional<WegSchematic> getSchematic(String name) throws IOException;

    WegSchematic createSchematic(String name, WegClipboard clipboard) throws IOException;

    boolean deleteSchematic(String name) throws IOException;

}
