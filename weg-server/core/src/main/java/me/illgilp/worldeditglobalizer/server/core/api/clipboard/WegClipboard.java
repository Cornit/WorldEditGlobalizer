package me.illgilp.worldeditglobalizer.server.core.api.clipboard;

import java.io.IOException;

public interface WegClipboard {

    int getHash();

    byte[] write() throws IOException;

    boolean isWegClipboard();

}
