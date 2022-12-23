package me.illgilp.worldeditglobalizer.proxy.core.server.connection;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import me.illgilp.worldeditglobalizer.common.permission.Permission;
import me.illgilp.worldeditglobalizer.common.scheduler.WegScheduler;
import me.illgilp.worldeditglobalizer.proxy.core.api.WegProxy;
import me.illgilp.worldeditglobalizer.proxy.core.api.server.ServerNotUsableException;
import me.illgilp.worldeditglobalizer.proxy.core.api.server.WegServer;
import me.illgilp.worldeditglobalizer.proxy.core.player.WegCorePlayer;

@RequiredArgsConstructor
public final class ServerConnectionListener {

    private final WegCorePlayer player;

    public void handleStateChange(WegServer.State from, WegServer.State to) {
        if (to == WegServer.State.USABLE) {
            if (WegProxy.getInstance().getProxyConfig().isClipboardAutoDownloadEnabled()) {
                WegScheduler.getInstance().getSyncExecutor().schedule(() -> {
                    if (player.hasPermission(Permission.CLIPBOARD_AUTO_DOWNLOAD, false)) {
                        try {
                            player.downloadClipboard();
                        } catch (ServerNotUsableException ignored) {
                        }
                    }
                }, 2500, TimeUnit.MILLISECONDS);
            }
        }
    }
}
