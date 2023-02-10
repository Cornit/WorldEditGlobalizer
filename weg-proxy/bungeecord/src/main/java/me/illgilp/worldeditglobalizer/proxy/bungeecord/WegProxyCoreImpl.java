package me.illgilp.worldeditglobalizer.proxy.bungeecord;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import me.illgilp.worldeditglobalizer.common.scheduler.WegSimpleScheduler;
import me.illgilp.worldeditglobalizer.proxy.bungeecord.player.WegCorePlayerImpl;
import me.illgilp.worldeditglobalizer.proxy.bungeecord.scheduler.WegSimpleSchedulerImpl;
import me.illgilp.worldeditglobalizer.proxy.bungeecord.server.WegCoreServerInfoImpl;
import me.illgilp.worldeditglobalizer.proxy.core.WegProxyCore;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.CommandSource;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegPlayer;
import me.illgilp.worldeditglobalizer.proxy.core.api.server.WegServerInfo;
import me.illgilp.worldeditglobalizer.proxy.core.player.WegCorePlayer;
import net.md_5.bungee.api.ProxyServer;

@RequiredArgsConstructor
public class WegProxyCoreImpl extends WegProxyCore {

    private final WorldEditGlobalizerPlugin plugin;

    @Override
    public File getDataFolder() {
        return this.plugin.getDataFolder();
    }

    @Override
    public InputStream getResource(String path) {
        return this.plugin.getResourceAsStream(path);
    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    @Override
    protected void onLoad() throws IOException {
        super.onLoad();
    }

    @Override
    protected void onEnable() {
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
        return Optional.ofNullable(ProxyServer.getInstance().getPlayer(uniqueId))
            .map(proxiedPlayer ->
                new WegCorePlayerImpl(
                    proxiedPlayer,
                    this.plugin.adventure().player(proxiedPlayer),
                    getGlobalSchematicContainer(),
                    getPacketSender()));
    }

    @Override
    protected Optional<WegCorePlayer> fetchPlayer(String name) {
        return Optional.ofNullable(ProxyServer.getInstance().getPlayer(name))
            .map(proxiedPlayer ->
                new WegCorePlayerImpl(
                    proxiedPlayer,
                    this.plugin.adventure().player(proxiedPlayer),
                    getGlobalSchematicContainer(),
                    getPacketSender()));
    }

    @Override
    protected Optional<WegServerInfo> fetchServerInfo(String name) {
        return Optional.ofNullable(ProxyServer.getInstance().getServerInfo(name))
            .map(WegCoreServerInfoImpl::new);
    }

    @Override
    protected Collection<String> getServerNames() {
        return ProxyServer.getInstance().getServers().keySet();
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
    protected boolean onPluginMessage(WegCorePlayer player, String channel, byte[] data) {
        return super.onPluginMessage(player, channel, data);
    }

    @Override
    protected void onCommand(CommandSource source, String commandLine) {
        super.onCommand(source, commandLine);
    }

    @Override
    protected List<String> onTabComplete(CommandSource source, String commandLine) {
        return super.onTabComplete(source, commandLine);
    }
}
