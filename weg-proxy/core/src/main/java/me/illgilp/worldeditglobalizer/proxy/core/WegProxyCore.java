package me.illgilp.worldeditglobalizer.proxy.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.Getter;
import me.illgilp.worldeditglobalizer.common.WorldEditGlobalizer;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;
import me.illgilp.worldeditglobalizer.common.network.PacketCallback;
import me.illgilp.worldeditglobalizer.common.permission.Permission;
import me.illgilp.worldeditglobalizer.common.scheduler.WegScheduler;
import me.illgilp.worldeditglobalizer.common.scheduler.WegSimpleScheduler;
import me.illgilp.worldeditglobalizer.common.util.yaml.YamlConfiguration;
import me.illgilp.worldeditglobalizer.proxy.core.api.WegProxy;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.CommandSource;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegOfflinePlayer;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegPlayer;
import me.illgilp.worldeditglobalizer.proxy.core.api.schematic.WegSchematicContainer;
import me.illgilp.worldeditglobalizer.proxy.core.api.server.WegServerInfo;
import me.illgilp.worldeditglobalizer.proxy.core.cache.OfflinePlayerCache;
import me.illgilp.worldeditglobalizer.proxy.core.command.SimpleCommandManager;
import me.illgilp.worldeditglobalizer.proxy.core.config.ProxyConfig;
import me.illgilp.worldeditglobalizer.proxy.core.player.WegCorePlayer;
import me.illgilp.worldeditglobalizer.proxy.core.schematic.WegGlobalSchematicContainer;
import me.illgilp.worldeditglobalizer.proxy.core.server.connection.ServerConnection;
import me.illgilp.worldeditglobalizer.proxy.core.task.ClipboardCleanupTask;
import me.illgilp.worldeditglobalizer.proxy.core.util.UpdateUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.NotNull;

public abstract class WegProxyCore implements WegProxy {

    private static WegProxyCore INSTANCE;
    protected final Map<UUID, WegCorePlayer> playersByUUID = new ConcurrentHashMap<>();
    protected final Map<String, WegCorePlayer> playersByName = new ConcurrentHashMap<>();
    protected final Map<String, WegServerInfo> servers = new ConcurrentHashMap<>();
    @Getter
    private ProxyConfig proxyConfig;

    private SimpleCommandManager commandManager;

    private WegSimpleScheduler scheduler;

    @Getter
    private final OfflinePlayerCache offlinePlayerCache;

    private final WegSchematicContainer globalSchematicContainer;

    public WegProxyCore() {
        INSTANCE = this;
        globalSchematicContainer = new WegGlobalSchematicContainer(() -> new File(getDataFolder(), "schematics"));
        offlinePlayerCache = new OfflinePlayerCache(getGlobalSchematicContainer());
    }

    public static WegProxyCore getInstance() {
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
        proxyConfig = new ProxyConfig(new YamlConfiguration(new File(getDataFolder(), "config.yaml")));
        this.loadConfiguration();
        this.offlinePlayerCache.load();
        this.commandManager = new SimpleCommandManager();
    }

    protected void onEnable() {
        new ClipboardCleanupTask(this).start();
    }

    protected void onDisable() {
        if (commandManager != null) {
            this.commandManager.shutdown();
        }
        if (scheduler != null) {
            try {
                scheduler.shutdown();
            } catch (InterruptedException e) {
                getLogger().log(Level.WARNING, "shutdown of WegSimpleScheduler interrupted", e);
            }
        }
    }

    protected final WegSchematicContainer getGlobalSchematicContainer() {
        return globalSchematicContainer;
    }

    protected abstract WegSimpleScheduler getNewScheduler();

    protected void onConfigReload() throws IOException {
        this.loadConfiguration();
    }

    protected void onPlayerConnected(WegPlayer player) {
        try {
            this.offlinePlayerCache.updatePlayer(player);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to update OfflinePlayerCache for player '" + player.getName() + "'", e);
        }
        WegScheduler.getInstance().getAsyncExecutor()
            .schedule(() -> {
                if (player.hasPermission(Permission.ADMIN_NOTIFY_UPDATE)) {
                    Optional<UpdateUtil.GithubRelease> update = UpdateUtil.getNewerRelease(
                        WorldEditGlobalizer.getBuildTag()
                    );
                    update.ifPresent(githubRelease ->
                        MessageHelper.builder()
                            .translation(TranslationKey.UPDATE_NOTIFICATION)
                            .tagResolver(Placeholder.unparsed("update_message", githubRelease.getName()))
                            .tagResolver(Placeholder.unparsed("current_version", WorldEditGlobalizer.getBuildTag()))
                            .sendMessageTo(player)
                    );
                }
            }, 2500, TimeUnit.MILLISECONDS);
    }

    protected void onPlayerDisconnected(WegPlayer player) {
        if (!getProxyConfig().isKeepClipboardEnabled()) {
            player.getClipboardContainer().clear();
        }
        removePlayer(player);
        try {
            this.offlinePlayerCache.updatePlayer(player);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to update OfflinePlayerCache for player '" + player.getName() + "'", e);
        }
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

    @Override
    public Optional<WegOfflinePlayer> getOfflinePlayer(UUID uniqueId) {
        Optional<WegPlayer> player = getPlayer(uniqueId);
        return player.<Optional<WegOfflinePlayer>>map(Optional::of).orElseGet(() -> this.offlinePlayerCache.getPlayer(uniqueId));
    }

    @Override
    public Optional<WegOfflinePlayer> getOfflinePlayer(String name) {
        Optional<WegPlayer> player = getPlayer(name);
        return player.<Optional<WegOfflinePlayer>>map(Optional::of).orElseGet(() -> this.offlinePlayerCache.getPlayer(name));
    }

    @Override
    public List<WegOfflinePlayer> getOfflinePlayersByNameStartingWith(String prefix) {
        return offlinePlayerCache.getByNameStartingWith(prefix);
    }

    private void loadConfiguration() throws IOException {
        createDefaultConfig("config.yaml");
        this.proxyConfig.load();
    }

    private void createDefaultConfig(String name) throws IOException {
        final File targetFile = new File(getDataFolder(), name);
        if (!targetFile.exists()) {
            try (InputStream stream = getResource("defaults/proxy/" + name)) {
                if (stream == null) {
                    throw new FileNotFoundException();
                }
                copyDefaultConfig(stream, targetFile, name);
            } catch (IOException e) {
                throw new IOException("Unable to read default configuration: " + name, e);
            }
        }
    }

    private void copyDefaultConfig(InputStream input, File actual, String name) throws IOException {
        if (actual.getParentFile() != null) {
            if (!actual.getParentFile().exists()) {
                actual.getParentFile().mkdirs();
            }
        }
        try (FileOutputStream output = new FileOutputStream(actual)) {
            try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buf = new byte[8192];
                int length;
                while ((length = input.read(buf)) > 0) {
                    baos.write(buf, 0, length);
                }
                String config = new String(baos.toByteArray(), StandardCharsets.UTF_8);
                config = config.replace(
                    "${random_secret}",
                    (UUID.randomUUID() + UUID.randomUUID().toString())
                        .replace("-", "")
                );
                output.write(config.getBytes(StandardCharsets.UTF_8));
                output.flush();
            }
        } catch (IOException e) {
            throw new IOException("Unable to write default configuration file", e);
        }
    }

    @Override
    public Optional<WegServerInfo> getServerInfo(String name) {
        Optional<WegServerInfo> info = Optional.ofNullable(this.servers.get(name));
        if (!info.isPresent()) {
            info = fetchServerInfo(name);
            info.ifPresent(wegServerInfo -> this.servers.put(wegServerInfo.getName(), wegServerInfo));
        }
        return info;
    }

    protected abstract Optional<WegServerInfo> fetchServerInfo(String name);

    @Override
    public Collection<WegServerInfo> getServers() {
        return getServerNames().stream()
            .map(this::getServerInfo)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
    }

    protected abstract Collection<String> getServerNames();

    public abstract File getDataFolder();

    public abstract InputStream getResource(String path);

    public abstract Logger getLogger();

    protected boolean onPluginMessage(WegCorePlayer player, String channel, byte[] data) {
        if (channel.equals(ServerConnection.PLUGIN_MESSAGE_CHANNEL.asString())) {
            if (player.getServerConnection() != null) {
                WegScheduler.getInstance().getAsyncPacketReadExecutor().execute(() -> {
                    try {
                        player.getServerConnection().handleBytes(data);
                    } catch (Exception e) {
                        WegProxy.getInstance().getLogger()
                            .log(Level.SEVERE, "Exception while handling packet", e);
                    }
                });
            }
            return true;
        }
        return false;
    }

    protected void onCommand(CommandSource source, String commandLine) {
        this.commandManager.handleCommandLine(source, commandLine);
    }

    protected List<String> onTabComplete(CommandSource source, String commandLine) {
        return this.commandManager.getSuggestions(source, commandLine);
    }

}
