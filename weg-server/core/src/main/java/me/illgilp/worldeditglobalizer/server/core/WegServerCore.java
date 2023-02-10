package me.illgilp.worldeditglobalizer.server.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.AccessLevel;
import lombok.Getter;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.network.PacketCallback;
import me.illgilp.worldeditglobalizer.common.network.PacketSender;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.KeepAlivePacket;
import me.illgilp.worldeditglobalizer.common.scheduler.WegScheduler;
import me.illgilp.worldeditglobalizer.common.scheduler.WegSimpleScheduler;
import me.illgilp.worldeditglobalizer.common.util.yaml.YamlConfiguration;
import me.illgilp.worldeditglobalizer.server.core.api.WegServer;
import me.illgilp.worldeditglobalizer.server.core.api.player.WegPlayer;
import me.illgilp.worldeditglobalizer.server.core.config.ServerConfig;
import me.illgilp.worldeditglobalizer.server.core.player.WegCorePlayer;
import me.illgilp.worldeditglobalizer.server.core.runnable.ClipboardAutoUploadRunnable;
import me.illgilp.worldeditglobalizer.server.core.server.connection.ServerConnection;
import org.jetbrains.annotations.NotNull;

public abstract class WegServerCore implements WegServer {

    private static WegServerCore INSTANCE;
    protected final ConcurrentHashMap<UUID, WegCorePlayer> playersByUUID = new ConcurrentHashMap<>();
    protected final Map<String, WegCorePlayer> playersByName = new ConcurrentHashMap<>();

    private WegSimpleScheduler scheduler;

    @Getter
    private ServerConfig serverConfig;

    @Getter(AccessLevel.PROTECTED)
    private PacketSender packetSender;

    public WegServerCore() {
        INSTANCE = this;
    }

    public static WegServerCore getInstance() {
        return INSTANCE;
    }

    protected void onLoad() throws IOException {
        this.scheduler = getNewScheduler();
        this.scheduler.getAsyncExecutor().scheduleAtFixedRate(PacketCallback::cleanup, 10, 10, TimeUnit.SECONDS);
        try {
            MessageHelper.loadMessages();
        } catch (IllegalAccessException | InstantiationException e) {
            getLogger().log(Level.SEVERE, "Failed to load messages", e);
            // TODO: disable plugin?!
        }
        serverConfig = new ServerConfig(new YamlConfiguration(new File(getDataFolder(), "config.yaml")));
        this.loadConfiguration();
        this.packetSender = new PacketSender();
        this.packetSender.start();
    }

    protected void onEnable() {
        WegScheduler.getInstance().getAsyncExecutor().scheduleAtFixedRate(
            new ClipboardAutoUploadRunnable(this),
            1,
            1,
            TimeUnit.SECONDS
        );
    }

    protected void onDisable() {
        if (scheduler != null) {
            try {
                scheduler.shutdown();
            } catch (InterruptedException e) {
                getLogger().log(Level.WARNING, "shutdown of WegSimpleScheduler interrupted");
            }
        }
    }

    protected abstract WegSimpleScheduler getNewScheduler();

    protected void onConfigReload() throws IOException {
        this.loadConfiguration();
    }

    protected void onPlayerConnected(WegPlayer player) {
        WegScheduler.getInstance().getAsyncExecutor().schedule(() -> {
            boolean failed = false;
            try {
                for (int i = 0; i < 50; i++) {
                    try {
                        PacketCallback.request(player.getConnection(), new KeepAlivePacket(), KeepAlivePacket.class)
                            .get(500, TimeUnit.MILLISECONDS);
                        if (failed) {
                            getLogger().info("Received initial KeepAlivePacket. -> Everything is fine now.");
                        }
                        return;
                    } catch (TimeoutException e) {
                        failed = true;
                        getLogger().warning("Sending initial KeepAlivePacket timed out. (This isn't an error!) -> Trying again in 0.1 seconds.");
                        TimeUnit.MILLISECONDS.sleep(100);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                getLogger().log(Level.SEVERE, "Exception while sending initial KeepAlivePacket", e);
            }
            if (failed) {
                getLogger().severe("Got no response for initial KeepAlivePacket after 50 tries. -> Plugin cannot be used on this server.");
            }
        }, 1000, TimeUnit.MILLISECONDS);

    }

    protected void onPlayerDisconnected(WegPlayer player) {
        removePlayer(player);
    }

    public Optional<WegCorePlayer> getCorePlayer(UUID uniqueId) {
        return getPlayerInternal(
            () -> playersByUUID.containsKey(uniqueId),
            () -> playersByUUID.get(uniqueId),
            () -> fetchPlayer(uniqueId)
        );
    }

    public Optional<WegPlayer> getPlayer(UUID uniqueId) {
        return getCorePlayer(uniqueId).map(wegCorePlayer -> wegCorePlayer);
    }

    public Optional<WegCorePlayer> getCorePlayer(String name) {
        return getPlayerInternal(
            () -> playersByName.containsKey(name),
            () -> playersByName.get(name),
            () -> fetchPlayer(name)
        );
    }

    public Optional<WegPlayer> getPlayer(String name) {
        return getCorePlayer(name).map(wegCorePlayer -> wegCorePlayer);
    }

    @NotNull
    private Optional<WegCorePlayer> getPlayerInternal(Supplier<Boolean> exists, Supplier<WegCorePlayer> getExisting, Supplier<Optional<WegCorePlayer>> fetchNewPlayer) {
        if (exists.get()) {
            return Optional.of(getExisting.get());
        }
        Optional<WegCorePlayer> player = fetchNewPlayer.get();
        player.ifPresent(this::addPlayer);
        return player;
    }

    public Collection<WegPlayer> getPlayers() {
        return Collections.unmodifiableCollection(this.playersByUUID.values());
    }

    protected abstract Optional<WegCorePlayer> fetchPlayer(UUID uniqueId);

    protected abstract Optional<WegCorePlayer> fetchPlayer(String name);

    private void addPlayer(WegCorePlayer player) {
        this.playersByUUID.put(player.getUniqueId(), player);
        this.playersByName.put(player.getName(), player);
    }

    private void removePlayer(WegPlayer player) {
        this.playersByUUID.remove(player.getUniqueId());
        this.playersByName.remove(player.getName());
    }


    private void loadConfiguration() throws IOException {
        createDefaultConfig("config.yaml");
        this.serverConfig.load();
    }

    private void createDefaultConfig(String name) throws IOException {
        final File targetFile = new File(getDataFolder(), name);
        if (!targetFile.exists()) {
            try (InputStream stream = getResource("defaults/server/" + name)) {
                if (stream == null) {
                    throw new FileNotFoundException();
                }
                copyDefaultConfig(stream, targetFile);
            } catch (IOException e) {
                throw new IOException("Unable to read default configuration: " + name, e);
            }
        }
    }

    private void copyDefaultConfig(InputStream input, File actual) throws IOException {
        if (actual.getParentFile() != null) {
            if (!actual.getParentFile().exists()) {
                actual.getParentFile().mkdirs();
            }
        }
        try (FileOutputStream output = new FileOutputStream(actual)) {
            byte[] buf = new byte[8192];
            int length;
            while ((length = input.read(buf)) > 0) {
                output.write(buf, 0, length);
            }
        } catch (IOException e) {
            throw new IOException("Unable to write default configuration file", e);
        }
    }

    public abstract File getDataFolder();

    public abstract InputStream getResource(String path);

    public abstract Logger getLogger();

    protected void onPluginMessage(WegCorePlayer player, String channel, byte[] data) {
        if (channel.equals(ServerConnection.PLUGIN_MESSAGE_CHANNEL.asString())) {
            if (player.getConnection() != null) {
                WegScheduler.getInstance().getAsyncPacketReadExecutor().execute(() -> {
                    try {
                        player.getServerConnection().handleBytes(data);
                    } catch (Exception e) {
                        WegServer.getInstance().getLogger()
                            .log(Level.SEVERE, "Exception while handling packet", e);
                    }
                });
            }
        }
    }


}
