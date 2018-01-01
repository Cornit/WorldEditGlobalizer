package me.illgilp.worldeditglobalizerbungee;

import me.illgilp.worldeditglobalizerbungee.commands.WEGCommand;
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
import me.illgilp.worldeditglobalizerbungee.network.packets.*;
import me.illgilp.yamlconfigurator.config.ConfigManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class WorldEditGlobalizerBungee extends Plugin {

    private static WorldEditGlobalizerBungee instance;

    private PacketManager packetManager;
    private ConfigManager configManager;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {

        packetManager = new PacketManager();
        configManager = new ConfigManager();
        configManager.addPlaceholder("{DATAFOLDER}",getDataFolder().getPath());
        configManager.registerConfig(new MainConfig());

        Metrics metrics = null;
        try {
            metrics = new Metrics(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(metrics != null){
            metrics.start();
        }

        packetManager.registerPacket(Packet.Direction.TO_BUNGEE, ClipboardSendPacket.class,0x0);
        packetManager.registerPacket(Packet.Direction.TO_BUKKIT, ClipboardSendPacket.class,0x1);
        packetManager.registerPacket(Packet.Direction.TO_BUNGEE, PermissionCheckRequestPacket.class, 0x2);
        packetManager.registerPacket(Packet.Direction.TO_BUKKIT, PermissionCheckResponsePacket.class, 0x3);
        packetManager.registerPacket(Packet.Direction.TO_BUNGEE, MessageRequestPacket.class, 0x4);
        packetManager.registerPacket(Packet.Direction.TO_BUKKIT, MessageResponsePacket.class, 0x5);
        packetManager.registerPacket(Packet.Direction.TO_BUNGEE, ClipboardRequestPacket.class,0x6);
        packetManager.registerPacket(Packet.Direction.TO_BUKKIT, ClipboardRequestPacket.class,0x7);
        packetManager.registerPacket(Packet.Direction.TO_BUNGEE, PluginConfigRequestPacket.class,0x8);
        packetManager.registerPacket(Packet.Direction.TO_BUKKIT, PluginConfigResponsePacket.class,0x9);
        packetManager.registerPacket(Packet.Direction.TO_BUNGEE, KeepAlivePacket.class,0x10);
        packetManager.registerPacket(Packet.Direction.TO_BUKKIT, KeepAlivePacket.class,0x10);

        getProxy().registerChannel("WorldEditGlobalizer");
        getProxy().getPluginManager().registerListener(this,new PluginMessageListener());
        getProxy().getPluginManager().registerListener(this, new PacketReceivedListener());
        getProxy().getPluginManager().registerListener(this, new PlayerQuitListener());
        getProxy().getPluginManager().registerListener(this, new ServerConnectedListener());

        String lang = WorldEditGlobalizerBungee.getInstance().getMainConfig().getLanguage();
        if(!MessageManager.getInstance().hasMessageFile(lang)){
            MessageFile file = new CustomMessageFile(lang,new File(MessageManager.getInstance().getMessageFolder(),"messages_"+lang+".yml"));
            MessageManager.getInstance().addMessageFile(file);
        }
        MessageManager.getInstance().setLanguage(lang);
        MessageManager.getInstance().setPrefix(ChatColor.translateAlternateColorCodes('&',WorldEditGlobalizerBungee.getInstance().getMainConfig().getPrefix()));


        getProxy().getPluginManager().registerCommand(this, new WEGCommand());
        CommandManager.getInstance().addCommand(new WEGSubCommands());

        if(!getMainConfig().isKeepClipboard()){
            ClipboardManager.getInstance().removeAll();
        }
    }

    @Override
    public void onDisable() {

    }

    public static WorldEditGlobalizerBungee getInstance() {
        return instance;
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
