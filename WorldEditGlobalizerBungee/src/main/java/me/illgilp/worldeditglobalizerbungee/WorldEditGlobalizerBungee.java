package me.illgilp.worldeditglobalizerbungee;

import com.j256.ormlite.logger.LocalLog;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.Callable;
import me.illgilp.mavenrepolib.lib.Library;
import me.illgilp.mavenrepolib.listener.ProgressListener;
import me.illgilp.mavenrepolib.repo.RemoteRepositories;
import me.illgilp.mavenrepolib.repo.RemoteRepository;
import me.illgilp.worldeditglobalizerbungee.commands.WEGCommand;
import me.illgilp.worldeditglobalizerbungee.commands.WEGSubCommands;
import me.illgilp.worldeditglobalizerbungee.commands.schematic.WEGSchematicCommands;
import me.illgilp.worldeditglobalizerbungee.config.MainConfig;
import me.illgilp.worldeditglobalizerbungee.listener.PacketReceivedListener;
import me.illgilp.worldeditglobalizerbungee.listener.PlayerJoinListener;
import me.illgilp.worldeditglobalizerbungee.listener.PlayerQuitListener;
import me.illgilp.worldeditglobalizerbungee.listener.PluginMessageListener;
import me.illgilp.worldeditglobalizerbungee.listener.ServerConnectedListener;
import me.illgilp.worldeditglobalizerbungee.manager.ClipboardManager;
import me.illgilp.worldeditglobalizerbungee.manager.CommandManager;
import me.illgilp.worldeditglobalizerbungee.manager.MessageManager;
import me.illgilp.worldeditglobalizerbungee.manager.SchematicManager;
import me.illgilp.worldeditglobalizerbungee.message.MessageFile;
import me.illgilp.worldeditglobalizerbungee.message.template.CustomMessageFile;
import me.illgilp.worldeditglobalizerbungee.metrics.Metrics;
import me.illgilp.worldeditglobalizerbungee.network.PacketManager;
import me.illgilp.worldeditglobalizerbungee.storage.Database;
import me.illgilp.worldeditglobalizerbungee.storage.table.UserCacheTable;
import me.illgilp.worldeditglobalizercommon.async.AsyncScheduler;
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
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;
import org.apache.commons.io.FileUtils;

public class WorldEditGlobalizerBungee extends Plugin {

    private static WorldEditGlobalizerBungee instance;

    private PacketManager packetManager;
    private ConfigManager configManager;
    private Database database;
    private boolean disabled = false;

    private AsyncScheduler asyncScheduler;

    public static WorldEditGlobalizerBungee getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "ERROR");
        instance = this;
        if ((!getDataFolder().exists()) && new File("plugins/WorldEditGlobalizerBungee").exists()) {
            new File("plugins/WorldEditGlobalizerBungee").renameTo(getDataFolder());
        }
        if (new File(getDataFolder(),"lib/sqlite-jdbc.jar").exists()) {
            new File(getDataFolder(),"lib/sqlite-jdbc.jar").delete();
        }
        RemoteRepository remoteRepository = new RemoteRepository(RemoteRepositories.MAVEN_CENTRAL, new File(getDataFolder(), "lib"));
        Library library = remoteRepository.getLibrary("org.xerial", "sqlite-jdbc", "3.31.1", new ProgressListener() {
            private int tries = 0;
            @Override
            public void onDone(File file) {
                getLogger().info("SQLite-JDBC driver downloaded!");
            }

            private String lastMSG = "";
            @Override
            public void onProgressChange(File file, long curr, long max) {
                if (new Double(((double)curr / (double)max * 100.0)).intValue() % 10 == 0) {
                    String msg = "Downloading... (" + new Double(((double) curr / (double) max * 100.0)).intValue() + " %)";
                    if (!lastMSG.equals(msg)) {
                        getLogger().info(msg);
                        lastMSG = msg;
                    }
                }
            }

            @Override
            public boolean onFailed(File file) {
                if (tries < 3) {
                    getLogger().info("Failed to download driver! Retry...");
                    tries++;
                } else {
                    Thread.currentThread().dumpStack();
                    getLogger().info("Failed to download driver! Please report it at https://github.com/IllgiLP/WorldEditGlobalizer/issues/new");
                }
                return tries < 3;
            }

            @Override
            public void onStart(File file) {
                getLogger().info("SQLite-JDBC driver not found! Downloading it...");
                getLogger().info("Please wait a moment, this can take a while.");
            }
        });
        if (library == null) {
            disabled = true;
            return;
        }

        library.addURLToClassPath((URLClassLoader) WorldEditGlobalizerBungee.class.getClassLoader());

    }

    @Override
    public void onEnable() {
        if (disabled) {
            getLogger().info("This plugin is now disabled!");
            return;
        }
        packetManager = new PacketManager();
        configManager = new ConfigManager();
        configManager.addPlaceholder("{DATAFOLDER}", getDataFolder().getPath());
        configManager.registerConfig(new MainConfig());

        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SingleLineChart("clipboards", () -> ClipboardManager.getInstance().getSavedClipboards().size()));
        metrics.addCustomChart(new Metrics.SingleLineChart("schematics", () -> SchematicManager.getInstance().getSchematics().size()));

        asyncScheduler = new AsyncScheduler(getLogger());
        BungeeCord.getInstance().getScheduler().runAsync(this, asyncScheduler::start);

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


        getProxy().registerChannel("weg:connection");
        getProxy().registerChannel("weg:ping");
        getProxy().getPluginManager().registerListener(this, new PluginMessageListener());
        getProxy().getPluginManager().registerListener(this, new PacketReceivedListener());
        getProxy().getPluginManager().registerListener(this, new PlayerQuitListener());
        getProxy().getPluginManager().registerListener(this, new ServerConnectedListener());
        getProxy().getPluginManager().registerListener(this, new PlayerJoinListener());

        String lang = WorldEditGlobalizerBungee.getInstance().getMainConfig().getLanguage();
        if (!MessageManager.getInstance().hasMessageFile(lang)) {
            MessageFile file = new CustomMessageFile(lang, new File(MessageManager.getInstance().getMessageFolder(), "messages_" + lang + ".yml"));
            MessageManager.getInstance().addMessageFile(file);
        }
        MessageManager.getInstance().setLanguage(lang);
        MessageManager.getInstance().setPrefix(ChatColor.translateAlternateColorCodes('&', WorldEditGlobalizerBungee.getInstance().getMainConfig().getPrefix()));

        database = new Database(new File(getDataFolder(), "worldeditglobalizer.sqlite"));
        database.registerTable(new UserCacheTable());
        try {
            database.initDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }


        getProxy().getPluginManager().registerCommand(this, new WEGCommand());
        CommandManager.getInstance().addCommand(new WEGSubCommands());
        CommandManager.getInstance().addSubCommand("schematic", new WEGSchematicCommands());

        if (!getMainConfig().isKeepClipboard()) {
            ClipboardManager.getInstance().removeAll();
        }

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

    public Database getDatabase() {
        return database;
    }

    public AsyncScheduler getAsyncScheduler() {
        return asyncScheduler;
    }

    private final void addURLToClassPath(URL url) throws IllegalAccessException, IllegalArgumentException, NoSuchMethodException, SecurityException, InvocationTargetException {
        URLClassLoader classLoader = (URLClassLoader) WorldEditGlobalizerBungee.class.getClassLoader();
        @SuppressWarnings("rawtypes")
        Class clazz = URLClassLoader.class;
        @SuppressWarnings("unchecked")
        Method method = clazz.getDeclaredMethod("addURL", new Class[] { URL.class });
        method.setAccessible(true);
        method.invoke(classLoader, new Object[] { url });
    }

}
