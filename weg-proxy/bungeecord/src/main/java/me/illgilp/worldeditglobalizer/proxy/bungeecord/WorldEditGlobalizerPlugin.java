package me.illgilp.worldeditglobalizer.proxy.bungeecord;

import me.illgilp.worldeditglobalizer.proxy.bungeecord.command.ConsoleCommandSource;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.CommandSource;
import me.illgilp.worldeditglobalizer.proxy.core.server.connection.ServerConnection;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.bstats.bungeecord.Metrics;

import java.io.IOException;

public class WorldEditGlobalizerPlugin extends Plugin implements Listener {

    private final WegProxyCoreImpl wegProxyCore;
    private BungeeAudiences adventure;

    private final int BSTATS_PLUGIN_ID = 3524;

    public WorldEditGlobalizerPlugin() {
        wegProxyCore = new WegProxyCoreImpl(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        try {
            this.wegProxyCore.onLoad();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BungeeAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
        }
        return this.adventure;
    }

    @Override
    public void onEnable() {
        this.adventure = BungeeAudiences.create(this);
        getProxy().registerChannel(ServerConnection.PLUGIN_MESSAGE_CHANNEL.asString());
        ProxyServer.getInstance().getPluginManager().registerListener(this, this);
        this.wegProxyCore.onEnable();
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Command("weg") {
            @Override
            public void execute(CommandSender sender, String[] args) {
                CommandSource commandSource;
                if (sender instanceof ProxiedPlayer) {
                    commandSource = wegProxyCore.getPlayer(((ProxiedPlayer) sender).getUniqueId())
                            .orElseThrow(() -> new IllegalStateException("strange state caught: could not find WegCorePlayer from online player"));
                } else {
                    commandSource = new ConsoleCommandSource(WorldEditGlobalizerPlugin.this.adventure().sender(sender));
                }
                wegProxyCore.onCommand(commandSource, "weg " + String.join(" ", args));
            }
        });
        new Metrics(this, BSTATS_PLUGIN_ID);
    }

    @Override
    public void onDisable() {
        this.wegProxyCore.onDisable();
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    @EventHandler
    public void onPlayerConnected(PostLoginEvent event) {
        this.wegProxyCore.onPlayerConnected(this.wegProxyCore.getPlayer(event.getPlayer().getUniqueId())
                .orElseThrow(() -> new IllegalStateException("could not get WegCorePlayer in PostLoginEvent")));
    }

    @EventHandler
    public void onPlayerDisconnected(PlayerDisconnectEvent event) {
        this.wegProxyCore.onPlayerDisconnected(this.wegProxyCore.getPlayer(event.getPlayer().getUniqueId())
                .orElseThrow(() -> new IllegalStateException("could not get WegCorePlayer in PlayerDisconnectEvent")));
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.getReceiver() instanceof ProxiedPlayer && event.getSender() instanceof Server) {
            boolean cancel = this.wegProxyCore.onPluginMessage(
                    wegProxyCore.getCorePlayer(((ProxiedPlayer) event.getReceiver()).getUniqueId())
                            .orElseThrow(() -> new IllegalStateException("strange state caught: could not find WegCorePlayer from online player")),
                    event.getTag(),
                    event.getData()
            );
            event.setCancelled(cancel);
        }
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent e) {
        final String commandLine = e.getCursor().startsWith("/") ? e.getCursor().substring(1) : e.getCursor();

        if (commandLine.startsWith("weg ")) {
            CommandSource commandSource;
            if (e.getSender() instanceof CommandSender) {
                if (e.getSender() instanceof ProxiedPlayer) {
                    commandSource = wegProxyCore.getPlayer(((ProxiedPlayer) e.getSender()).getUniqueId())
                            .orElseThrow(() -> new IllegalStateException("strange state caught: could not find WegCorePlayer from online player"));
                } else {
                    commandSource = new ConsoleCommandSource(WorldEditGlobalizerPlugin.this.adventure().sender((CommandSender) e.getSender()));
                }
                e.getSuggestions().clear();
                e.getSuggestions().addAll(this.wegProxyCore.onTabComplete(commandSource, commandLine));
            }
        }
    }
}
