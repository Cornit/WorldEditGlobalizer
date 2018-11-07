package me.illgilp.worldeditglobalizersponge;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.sponge.SpongeWorldEdit;
import me.illgilp.worldeditglobalizersponge.listener.PacketReceivedListener;
import me.illgilp.worldeditglobalizersponge.listener.PlayerJoinListener;
import me.illgilp.worldeditglobalizersponge.listener.PluginMessageListener;
import me.illgilp.worldeditglobalizersponge.network.PacketManager;
import me.illgilp.worldeditglobalizersponge.network.packets.*;
import me.illgilp.worldeditglobalizersponge.runnables.ClipboardRunnable;
import me.illgilp.worldeditglobalizersponge.task.AsyncTask;
import me.illgilp.worldeditglobalizersponge.task.QueuedAsyncTask;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Plugin(id="worldeditglobalizersponge", name = "WorldEditGlobalizer", version = "1.2.1", authors = "IllgiLP")
public class WorldEditGlobalizerSponge {

    private static WorldEditGlobalizerSponge instance;
    private PacketManager packetManager;
    private ChannelBinding.RawDataChannel dataChannel;



    @Listener
    public void onLoad(GamePreInitializationEvent e) {
        instance = this;
    }


    @Listener
    public void onEnable(GameInitializationEvent e) {
       PluginContainer spongeWorldEdit = Sponge.getPluginManager().getPlugin("worldedit").get();
        if(spongeWorldEdit == null){
            System.out.println("WorldEdit not found! Disable plugin!");
            return;
        }
        dataChannel = Sponge.getChannelRegistrar().getOrCreateRaw(this,"WorldEditGlobalizer");
        dataChannel.addListener(new PluginMessageListener());
        packetManager = new PacketManager();
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

        Sponge.getEventManager().registerListeners(this, new PacketReceivedListener());
        Sponge.getEventManager().registerListeners(this, new PlayerJoinListener());


        for(Player p : Sponge.getServer().getOnlinePlayers()){
            AsyncTask rn = new ClipboardRunnable(p);
            rn.start();
        }
    }

    @Listener
    public void onDisable(GameStoppingEvent e) {

    }


    public static WorldEditGlobalizerSponge getInstance() {
        return instance;
    }

    public PacketManager getPacketManager() {
        return packetManager;
    }

    public ChannelBinding.RawDataChannel getDataChannel() {
        return dataChannel;
    }

    public SpongeWorldEdit getSpongeWorldEdit() {
        return SpongeWorldEdit.inst();
    }

}
