package me.illgilp.worldeditglobalizer.proxy.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.CommandSource;
import me.illgilp.worldeditglobalizer.proxy.core.server.connection.ServerConnection;
import me.illgilp.worldeditglobalizer.proxy.velocity.command.ConsoleCommandSource;
import org.bstats.velocity.Metrics;

@Plugin(
    id = "worldeditglobalizer",
    name = "WorldEditGlobalizer",
    authors = { "IllgiLP" },
    url = "https://github.com/IllgiLP/WorldEditGlobalizer",
    description = "You want to share your clipboard over servers? This is the right tool to do that!",
    version = "@version@"
)
public class WorldEditGlobalizerPlugin {

    private final WegProxyCoreImpl wegProxyCore;
    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;
    private final Metrics.Factory metricsFactory;

    private final int BSTATS_PLUGIN_ID = 17691;

    @Inject
    public WorldEditGlobalizerPlugin(
        ProxyServer proxy,
        Logger logger,
        @DataDirectory Path dataFolderPath,
        Metrics.Factory metricsFactory
    ) {
        this.proxyServer = proxy;
        this.logger = logger;
        this.dataDirectory = dataFolderPath;
        this.metricsFactory = metricsFactory;
        wegProxyCore = new WegProxyCoreImpl(this);
        try {
            this.wegProxyCore.onLoad();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        getProxyServer().getChannelRegistrar()
            .register(MinecraftChannelIdentifier.from(ServerConnection.PLUGIN_MESSAGE_CHANNEL.asString()));
        this.wegProxyCore.onEnable();
        CommandManager commandManager = getProxyServer().getCommandManager();
        commandManager.register(
            commandManager.metaBuilder("weg")
                .plugin(this)
                .build(),
            (SimpleCommand) invocation -> {
                CommandSource commandSource;
                if (invocation.source() instanceof Player) {
                    commandSource = wegProxyCore.getPlayer(
                            ((Player) invocation.source()).getUniqueId())
                        .orElseThrow(() -> new IllegalStateException("strange state caught: could not find WegCorePlayer from online player"));
                } else {
                    commandSource = new ConsoleCommandSource(getProxyServer().getConsoleCommandSource());
                }
                wegProxyCore.onCommand(commandSource, "weg " + String.join(" ", invocation.arguments()));
            }
        );
        metricsFactory.make(this, BSTATS_PLUGIN_ID);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.wegProxyCore.onDisable();
    }


    @Subscribe
    public void onPlayerConnected(PostLoginEvent event) {
        this.wegProxyCore.onPlayerConnected(this.wegProxyCore.getPlayer(event.getPlayer().getUniqueId())
            .orElseThrow(() -> new IllegalStateException("could not get WegCorePlayer in PostLoginEvent")));
    }

    @Subscribe
    public void onPlayerDisconnected(DisconnectEvent event) {
        this.wegProxyCore.onPlayerDisconnected(this.wegProxyCore.getPlayer(event.getPlayer().getUniqueId())
            .orElseThrow(() -> new IllegalStateException("could not get WegCorePlayer in PlayerDisconnectEvent")));
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.getTarget() instanceof Player && event.getSource() instanceof com.velocitypowered.api.proxy.ServerConnection) {
            boolean cancel = this.wegProxyCore.onPluginMessage(
                wegProxyCore.getCorePlayer(((Player) event.getTarget()).getUniqueId())
                    .orElseThrow(() -> new IllegalStateException("strange state caught: could not find WegCorePlayer from online player")),
                event.getIdentifier().getId(),
                event.getData()
            );
            event.setResult(cancel ? PluginMessageEvent.ForwardResult.handled() : PluginMessageEvent.ForwardResult.forward());
        }
    }

    @Subscribe
    public void onTabComplete(TabCompleteEvent e) {
        CommandSource commandSource;
        commandSource = wegProxyCore.getPlayer(e.getPlayer().getUniqueId())
            .orElseThrow(() -> new IllegalStateException("strange state caught: could not find WegCorePlayer from online player"));

        e.getSuggestions().clear();
        e.getSuggestions().addAll(this.wegProxyCore.onTabComplete(
            commandSource,
            e.getPartialMessage().startsWith("/") ? e.getPartialMessage().substring(1) : e.getPartialMessage()
        ));
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    public Logger getLogger() {
        return logger;
    }

    public File getDataFolder() {
        return dataDirectory.toFile();
    }
}
