package me.illgilp.worldeditglobalizer.server.core.runnable;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.illgilp.worldeditglobalizer.server.core.api.WegServer;
import me.illgilp.worldeditglobalizer.server.core.api.clipboard.WegClipboard;
import me.illgilp.worldeditglobalizer.server.core.api.player.WegPlayer;
import me.illgilp.worldeditglobalizer.server.core.server.WegServerConnection;

public class ClipboardAutoUploadRunnable implements Runnable {

    private final WegServer wegServer;
    private final Map<UUID, Integer> lastHashes = new ConcurrentHashMap<>();

    public ClipboardAutoUploadRunnable(WegServer wegServer) {
        this.wegServer = wegServer;
    }

    @Override
    public void run() {
        for (WegPlayer wegPlayer : new ArrayList<>(wegServer.getPlayers())) {
            if (wegPlayer.getConnection().getState() != WegServerConnection.State.USABLE) {
                continue;
            }
            if (wegPlayer.getProxyConfig().isClipboardAutoUploadEnabled()) {
                if (wegPlayer.getClipboard().isPresent()) {
                    WegClipboard clipboard = wegPlayer.getClipboard().get();
                    if (clipboard.isWegClipboard()) {
                        continue;
                    }
                    if (lastHashes.containsKey(wegPlayer.getUniqueId())) {
                        if (lastHashes.get(wegPlayer.getUniqueId()) == clipboard.getHash()) {
                            continue;
                        }
                    }
                    lastHashes.put(wegPlayer.getUniqueId(), clipboard.getHash());
                    wegPlayer.uploadClipboard();
                }
            }
        }
    }
}
