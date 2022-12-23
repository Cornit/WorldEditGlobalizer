package me.illgilp.worldeditglobalizer.proxy.core.api.clipboard;

import java.io.IOException;
import java.time.Instant;

public interface WegClipboard {

    Instant getUploadDate();

    long getSize();

    int getHash() throws IOException;

    byte[] getData() throws IOException;

}
