package me.illgilp.worldeditglobalizer.server.bukkit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import me.illgilp.worldeditglobalizer.common.scheduler.WegSimpleScheduler;
import me.illgilp.worldeditglobalizer.server.bukkit.api.worldedit.adapter.WorldEditAdapter;
import me.illgilp.worldeditglobalizer.server.bukkit.api.worldedit.adapter.WorldEditAdapterFilter;
import me.illgilp.worldeditglobalizer.server.bukkit.player.WegCorePlayerImpl;
import me.illgilp.worldeditglobalizer.server.bukkit.scheduler.WegSimpleSchedulerImpl;
import me.illgilp.worldeditglobalizer.server.core.WegServerCore;
import me.illgilp.worldeditglobalizer.server.core.api.player.WegPlayer;
import me.illgilp.worldeditglobalizer.server.core.player.WegCorePlayer;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;


@RequiredArgsConstructor
public class WegServerCoreImpl extends WegServerCore {

    private final WorldEditGlobalizerPlugin plugin;

    @Override
    public File getDataFolder() {
        return this.plugin.getDataFolder();
    }

    @Override
    public InputStream getResource(String path) {
        return this.plugin.getResource(path);
    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    @Override
    public Audience getConsoleAudience() {
        return plugin.adventure().console();
    }

    @Override
    protected void onLoad() throws IOException {
        super.onLoad();
    }

    @Override
    protected void onEnable() {
        WorldEditAdapter worldEditAdapter = WorldEditAdapterFilter.getWorldEditAdapter();
        if (worldEditAdapter == null) {
            getLogger().severe("Could not detect matching WorldEditAdapter");
        } else {
            getLogger().info("Using WorldEditAdapter for minecraft version '"
                + worldEditAdapter.getFilter().getMinMinecraftVersion() + "', WorldEditPlugin '"
                + worldEditAdapter.getFilter().getWorldEditPluginType().getPluginName() + "' and WorldEditPlugin version '"
                + worldEditAdapter.getFilter().getWorldEditPluginVersion() + "'");
        }
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        super.onDisable();
    }

    @Override
    protected WegSimpleScheduler getNewScheduler() {
        return new WegSimpleSchedulerImpl(plugin);
    }

    @Override
    protected void onConfigReload() throws IOException {
        super.onConfigReload();
    }

    @Override
    protected Optional<WegCorePlayer> fetchPlayer(UUID uniqueId) {
        return Optional.ofNullable(Bukkit.getPlayer(uniqueId))
            .map(player -> new WegCorePlayerImpl(player, this.plugin.adventure().player(player), plugin));
    }

    @Override
    protected Optional<WegCorePlayer> fetchPlayer(String name) {
        return Optional.ofNullable(Bukkit.getPlayer(name))
            .map(player -> new WegCorePlayerImpl(player, this.plugin.adventure().player(player), plugin));
    }

    @Override
    protected void onPlayerConnected(WegPlayer player) {
        super.onPlayerConnected(player);
    }

    @Override
    protected void onPlayerDisconnected(WegPlayer player) {
        super.onPlayerDisconnected(player);
    }

    @Override
    protected void onPluginMessage(WegCorePlayer player, String channel, byte[] data) {
        super.onPluginMessage(player, channel, data);
    }
}
