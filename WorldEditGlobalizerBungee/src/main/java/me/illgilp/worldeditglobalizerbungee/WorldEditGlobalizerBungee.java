package me.illgilp.worldeditglobalizerbungee;

import java.io.File;
import me.illgilp.worldeditglobalizerbungee.commands.WEGCommand;
import me.illgilp.worldeditglobalizerbungee.commands.WEGSchematicCommands;
import me.illgilp.worldeditglobalizerbungee.commands.WEGSubCommands;
import me.illgilp.worldeditglobalizerbungee.config.MainConfig;
import me.illgilp.worldeditglobalizerbungee.listener.PacketReceivedListener;
import me.illgilp.worldeditglobalizerbungee.listener.PlayerQuitListener;
import me.illgilp.worldeditglobalizerbungee.listener.PluginMessageListener;
import me.illgilp.worldeditglobalizerbungee.listener.ServerConnectedListener;
import me.illgilp.worldeditglobalizerbungee.manager.ClipboardManager;
import me.illgilp.worldeditglobalizerbungee.manager.CommandManager;
import me.illgilp.worldeditglobalizerbungee.manager.MessageManager;
import me.illgilp.worldeditglobalizerbungee.message.MessageFile;
import me.illgilp.worldeditglobalizerbungee.message.template.CustomMessageFile;
import me.illgilp.worldeditglobalizerbungee.metrics.Metrics;
import me.illgilp.worldeditglobalizerbungee.network.PacketManager;
import me.illgilp.worldeditglobalizerbungee.runnables.UpdateRunnable;
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
import me.illgilp.yamlconfigurator.config.ConfigManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;

public class WorldEditGlobalizerBungee extends Plugin {

    private static WorldEditGlobalizerBungee instance;

    private PacketManager packetManager;
    private ConfigManager configManager;

    public static WorldEditGlobalizerBungee getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        if ((!getDataFolder().exists()) && new File("plugins/WorldEditGlobalizerBungee").exists()) {
            new File("plugins/WorldEditGlobalizerBungee").renameTo(getDataFolder());
        }
    }

    @Override
    public void onEnable() {

        packetManager = new PacketManager();
        configManager = new ConfigManager();
        configManager.addPlaceholder("{DATAFOLDER}", getDataFolder().getPath());
        configManager.registerConfig(new MainConfig());

        Metrics metrics = new Metrics(this);
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

        getProxy().registerChannel("worldeditglobalizer:connection");
        getProxy().getPluginManager().registerListener(this, new PluginMessageListener());
        getProxy().getPluginManager().registerListener(this, new PacketReceivedListener());
        getProxy().getPluginManager().registerListener(this, new PlayerQuitListener());
        getProxy().getPluginManager().registerListener(this, new ServerConnectedListener());

        String lang = WorldEditGlobalizerBungee.getInstance().getMainConfig().getLanguage();
        if (!MessageManager.getInstance().hasMessageFile(lang)) {
            MessageFile file = new CustomMessageFile(lang, new File(MessageManager.getInstance().getMessageFolder(), "messages_" + lang + ".yml"));
            MessageManager.getInstance().addMessageFile(file);
        }
        MessageManager.getInstance().setLanguage(lang);
        MessageManager.getInstance().setPrefix(ChatColor.translateAlternateColorCodes('&', WorldEditGlobalizerBungee.getInstance().getMainConfig().getPrefix()));


        getProxy().getPluginManager().registerCommand(this, new WEGCommand());
        CommandManager.getInstance().addCommand(new WEGSubCommands());
        CommandManager.getInstance().addSubCommand("schematic", new WEGSchematicCommands());

        if (!getMainConfig().isKeepClipboard()) {
            ClipboardManager.getInstance().removeAll();
        }

        BungeeCord.getInstance().getScheduler().runAsync(this, new UpdateRunnable());

    }

    @Override
    public void onDisable() {

    }

    public PacketManager getPacketManager() {
        return packetManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MainConfig getMainConfig() {
        return (MainConfig) configManager.getConfig("MainConfig");
    }
}
