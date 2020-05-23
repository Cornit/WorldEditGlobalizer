package me.illgilp.worldeditglobalizerbukkit;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import java.io.File;
import me.illgilp.worldeditglobalizerbukkit.config.MainConfig;
import me.illgilp.worldeditglobalizerbukkit.listener.PacketReceivedListener;
import me.illgilp.worldeditglobalizerbukkit.listener.PlayerConnectionListener;
import me.illgilp.worldeditglobalizerbukkit.listener.PluginMessageListener;
import me.illgilp.worldeditglobalizerbukkit.manager.VersionManager;
import me.illgilp.worldeditglobalizerbukkit.network.PacketManager;
import me.illgilp.worldeditglobalizercommon.network.packets.ClipboardRequestPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.ClipboardSendPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.KeepAlivePacket;
import me.illgilp.worldeditglobalizercommon.network.packets.MessageRequestPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.MessageResponsePacket;
import me.illgilp.worldeditglobalizercommon.network.packets.Packet;
import me.illgilp.worldeditglobalizercommon.network.packets.PermissionCheckRequestPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.PermissionCheckResponsePacket;
import me.illgilp.worldeditglobalizercommon.network.packets.PluginConfigRequestPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.PluginConfigResponsePacket;
import me.illgilp.worldeditglobalizercommon.network.packets.PluginSendPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.PluginSendResultPacket;
import me.illgilp.yamlconfigurator.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldEditGlobalizerBukkit extends JavaPlugin {

    private static WorldEditGlobalizerBukkit instance;
    private PacketManager packetManager;
    private WorldEditPlugin worldEditPlugin;

    private ConfigManager configManager;

    private boolean usable = true;

    public static WorldEditGlobalizerBukkit getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        configManager = new ConfigManager();
        configManager.addPlaceholder("{DATAFOLDER}", getDataFolder().getPath());
        configManager.registerConfig(new MainConfig());
        VersionManager versionManager = VersionManager.getInstance();
        if (versionManager.getMinecraftVersion() == null) {
            getLogger().severe("Cannot detect minecraft version! Found: '" + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] +  "' Disable plugin!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (versionManager.getUsedVersion() == null) {
            getLogger().severe("Unsupported minecraft version! Found: '" + versionManager.getMinecraftVersion() +  "' Disable plugin!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (versionManager.getWorldEditManager() == null) {
            getLogger().severe("Cannot create WorldEditManager! Disable plugin!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (worldEditPlugin == null) {
            getLogger().severe("WorldEdit not found! Disable plugin!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("Minecraft version " + versionManager.getMinecraftVersion() + " found! Using: " + versionManager.getUsedVersion());
        PluginMessageListener pluginMessageListener = new PluginMessageListener();
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "weg:connection");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "weg:connection", pluginMessageListener);

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "weg:ping");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "weg:ping", pluginMessageListener);

        packetManager = new PacketManager();
        packetManager.registerPacket(Packet.Direction.TO_BUNGEE, ClipboardSendPacket.class, 0x0);
        packetManager.registerPacket(Packet.Direction.TO_BUKKIT, ClipboardSendPacket.class, 0x1);
        packetManager.registerPacket(Packet.Direction.TO_BUNGEE, PermissionCheckRequestPacket.class, 0x2);
        packetManager.registerPacket(Packet.Direction.TO_BUKKIT, PermissionCheckResponsePacket.class, 0x3);
        packetManager.registerPacket(Packet.Direction.TO_BUNGEE, MessageRequestPacket.class, 0x4);
        packetManager.registerPacket(Packet.Direction.TO_BUKKIT, MessageResponsePacket.class, 0x5);
        packetManager.registerPacket(Packet.Direction.TO_BUNGEE, ClipboardRequestPacket.class, 0x6);
        packetManager.registerPacket(Packet.Direction.TO_BUKKIT, ClipboardRequestPacket.class, 0x7);
        packetManager.registerPacket(Packet.Direction.TO_BUNGEE, PluginConfigRequestPacket.class, 0x8);
        packetManager.registerPacket(Packet.Direction.TO_BUKKIT, PluginConfigResponsePacket.class, 0x9);
        packetManager.registerPacket(Packet.Direction.TO_BUNGEE, KeepAlivePacket.class, 0x10);
        packetManager.registerPacket(Packet.Direction.TO_BUKKIT, KeepAlivePacket.class, 0x10);
        packetManager.registerPacket(Packet.Direction.TO_BUKKIT, PluginSendPacket.class, 0x11);
        packetManager.registerPacket(Packet.Direction.TO_BUNGEE, PluginSendResultPacket.class, 0x11);

        Bukkit.getPluginManager().registerEvents(new PacketReceivedListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(), this);
        for (Player p : Bukkit.getOnlinePlayers()) {
            VersionManager.getInstance().getWorldEditManager().startClipboardRunnable(p);
        }



    }

    @Override
    public void onDisable() {


    }

    public File getFile() {
        return super.getFile();
    }

    public PacketManager getPacketManager() {
        return packetManager;
    }

    public WorldEditPlugin getWorldEditPlugin() {
        return worldEditPlugin;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MainConfig getMainConfig() {
        return (MainConfig) configManager.getConfig("MainConfig");
    }

    public boolean isAsyncWorldEdit() {
        return Bukkit.getPluginManager().isPluginEnabled("AsyncWorldEdit");
    }
    public boolean isFastAsyncWorldEdit() {
        return Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit");
    }

    public boolean isUsable() {
        return usable;
    }

    public void setUsable(boolean usable) {
        if (this.usable && !usable) {
            VersionManager.getInstance().getWorldEditManager().cancelAllClipboardRunnable();
        } else if (!this.usable && usable) {
            this.usable = true;
            for (Player p : Bukkit.getOnlinePlayers()) {
                VersionManager.getInstance().getWorldEditManager().startClipboardRunnable(p);
            }
        }
        this.usable = usable;
    }
}
