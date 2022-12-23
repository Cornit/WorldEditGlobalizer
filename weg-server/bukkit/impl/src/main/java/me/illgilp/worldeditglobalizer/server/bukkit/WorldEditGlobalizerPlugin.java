package me.illgilp.worldeditglobalizer.server.bukkit;

import java.io.IOException;
import me.illgilp.worldeditglobalizer.server.core.server.connection.ServerConnection;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldEditGlobalizerPlugin extends JavaPlugin implements Listener {

    private final WegServerCoreImpl wegServerCore;
    private BukkitAudiences adventure;

    public WorldEditGlobalizerPlugin() {
        wegServerCore = new WegServerCoreImpl(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        try {
            this.wegServerCore.onLoad();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
        }
        return this.adventure;
    }

    @Override
    public void onEnable() {
        this.adventure = BukkitAudiences.create(this);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, ServerConnection.PLUGIN_MESSAGE_CHANNEL.asString(), this::onPluginMessage);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, ServerConnection.PLUGIN_MESSAGE_CHANNEL.asString());
        Bukkit.getPluginManager().registerEvents(this, this);
        this.wegServerCore.onEnable();
    }

    @Override
    public void onDisable() {
        this.wegServerCore.onDisable();
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    @EventHandler
    public void onPlayerConnected(PlayerJoinEvent event) {
        this.wegServerCore.onPlayerConnected(this.wegServerCore.getPlayer(event.getPlayer().getUniqueId())
            .orElseThrow(() -> new IllegalStateException("could not get WegCorePlayer in PlayerJoinEvent")));
    }

    @EventHandler
    public void onPlayerDisconnected(PlayerQuitEvent event) {
        this.wegServerCore.onPlayerDisconnected(this.wegServerCore.getPlayer(event.getPlayer().getUniqueId())
            .orElseThrow(() -> new IllegalStateException("could not get WegCorePlayer in PlayerQuitEvent")));
    }

    private void onPluginMessage(String s, Player player, byte[] bytes) {
        this.wegServerCore.onPluginMessage(
            wegServerCore.getCorePlayer(player.getUniqueId())
                .orElseThrow(() -> new IllegalStateException("strange state caught: could not find WegCorePlayer from online player")),
            s,
            bytes
        );
    }


}
