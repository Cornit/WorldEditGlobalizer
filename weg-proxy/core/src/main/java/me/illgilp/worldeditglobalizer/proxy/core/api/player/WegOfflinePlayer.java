package me.illgilp.worldeditglobalizer.proxy.core.api.player;

import java.util.UUID;
import me.illgilp.worldeditglobalizer.proxy.core.api.clipboard.WegClipboardContainer;
import me.illgilp.worldeditglobalizer.proxy.core.api.schematic.WegSchematicContainer;

public interface WegOfflinePlayer {

    UUID getUniqueId();

    String getName();

    boolean isOnline();

    WegClipboardContainer getClipboardContainer();

    WegSchematicContainer getSchematicContainer();

}
