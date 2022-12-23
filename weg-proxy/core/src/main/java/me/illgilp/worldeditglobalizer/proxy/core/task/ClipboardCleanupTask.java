package me.illgilp.worldeditglobalizer.proxy.core.task;

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.illgilp.worldeditglobalizer.common.scheduler.WegScheduler;
import me.illgilp.worldeditglobalizer.proxy.core.WegProxyCore;
import me.illgilp.worldeditglobalizer.proxy.core.api.clipboard.WegClipboardContainer;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegOfflinePlayer;

public class ClipboardCleanupTask implements Runnable {

    private final WegProxyCore proxy;

    private boolean running = false;
    private final File clipboardsFolder;
    private final Pattern uuidPattern = Pattern.compile("^([a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12})\\.clipboard$");

    public ClipboardCleanupTask(WegProxyCore proxy) {
        this.proxy = proxy;
        this.clipboardsFolder = new File(proxy.getDataFolder(), "clipboards");
    }

    @Override
    public void run() {
        if (this.clipboardsFolder.exists()
            && this.clipboardsFolder.isDirectory()
            && proxy.getProxyConfig().isClipboardAutoCleanupEnabled()
        ) {
            proxy.getOfflinePlayerCache().cleanup();
            for (File file : Objects.requireNonNull(this.clipboardsFolder.listFiles())) {
                final Matcher matcher = this.uuidPattern.matcher(file.getName());
                if (matcher.matches()) {
                    UUID uuid;
                    try {
                        uuid = UUID.fromString(matcher.group(1));
                    } catch (IllegalArgumentException e) {
                        this.proxy.getLogger().log(Level.SEVERE, "Could not convert string to uuid", e);
                        continue;
                    }
                    final Optional<WegOfflinePlayer> offlinePlayer = this.proxy.getOfflinePlayer(uuid);
                    if (!offlinePlayer.isPresent()) {
                        final WegClipboardContainer clipboardContainer = new WegClipboardContainer(uuid);
                        if (clipboardContainer.hasClipboard()) {
                            clipboardContainer.clear();
                            proxy.getLogger().info("Deleted clipboard for player with uuid: " + uuid);
                        }
                    }
                }
            }
        }
    }


    public boolean start() {
        if (running) {
            return false;
        }
        running = true;
        WegScheduler.getInstance().getAsyncExecutor()
            .scheduleAtFixedRate(this, 0, 60, TimeUnit.MINUTES);
        return true;
    }
}
