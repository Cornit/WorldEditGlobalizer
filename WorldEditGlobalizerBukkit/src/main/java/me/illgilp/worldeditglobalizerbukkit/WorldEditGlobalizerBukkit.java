package me.illgilp.worldeditglobalizerbukkit;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.illgilp.worldeditglobalizerbukkit.listener.PacketReceivedListener;
import me.illgilp.worldeditglobalizerbukkit.listener.PlayerConnectionListener;
import me.illgilp.worldeditglobalizerbukkit.listener.PluginMessageListener;
import me.illgilp.worldeditglobalizerbukkit.network.PacketManager;
import me.illgilp.worldeditglobalizerbukkit.runnables.AWEClipboardRunnable;
import me.illgilp.worldeditglobalizerbukkit.runnables.ClipboardRunnable;
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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldEditGlobalizerBukkit extends JavaPlugin {

    private static WorldEditGlobalizerBukkit instance;
    private PacketManager packetManager;
    private WorldEditPlugin worldEditPlugin;

    public static WorldEditGlobalizerBukkit getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (worldEditPlugin == null) {
            getLogger().info("WorldEdit not found! Disable plugin!");
            Bukkit.getPluginManager().disablePlugin(this);
        }
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "worldeditglobalizer:connection");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "worldeditglobalizer:connection", new PluginMessageListener());

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

        Bukkit.getPluginManager().registerEvents(new PacketReceivedListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(), this);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isAsyncWorldEdit()) {
                new AWEClipboardRunnable(p).runTaskTimerAsynchronously(this, 20, 20);
            } else {
                new ClipboardRunnable(p).runTaskTimerAsynchronously(this, 20, 20);
            }
        }


    }

    @Override
    public void onDisable() {


    }

    public PacketManager getPacketManager() {
        return packetManager;
    }

    public WorldEditPlugin getWorldEditPlugin() {
        return worldEditPlugin;
    }

    public boolean isAsyncWorldEdit() {
        return Bukkit.getPluginManager().isPluginEnabled("AsyncWorldEdit");
    }
}
