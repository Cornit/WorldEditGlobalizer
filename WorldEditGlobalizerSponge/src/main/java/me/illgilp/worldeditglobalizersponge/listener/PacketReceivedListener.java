package me.illgilp.worldeditglobalizersponge.listener;

import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.sponge.SpongePlayer;
import com.sk89q.worldedit.world.registry.LegacyWorldData;
import me.illgilp.worldeditglobalizersponge.WorldEditGlobalizerSponge;
import me.illgilp.worldeditglobalizersponge.clipboard.WEGBlockArrayClipboard;
import me.illgilp.worldeditglobalizersponge.clipboard.WEGClipboardHolder;
import me.illgilp.worldeditglobalizersponge.clipboard.WEGSchematicReader;
import me.illgilp.worldeditglobalizersponge.clipboard.WEGSchematicWriter;
import me.illgilp.worldeditglobalizersponge.events.PacketReceivedEvent;
import me.illgilp.worldeditglobalizersponge.manager.ConfigManager;
import me.illgilp.worldeditglobalizersponge.manager.MessageManager;
import me.illgilp.worldeditglobalizersponge.manager.PermissionManager;
import me.illgilp.worldeditglobalizersponge.network.PacketSender;
import me.illgilp.worldeditglobalizersponge.network.packets.*;
import me.illgilp.worldeditglobalizersponge.runnables.ClipboardRunnable;
import me.illgilp.worldeditglobalizersponge.runnables.PacketRunnable;
import me.illgilp.worldeditglobalizersponge.task.QueuedAsyncTask;
import me.illgilp.worldeditglobalizersponge.util.PacketDataSerializer;
import me.illgilp.worldeditglobalizersponge.util.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.scheduler.Task;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PacketReceivedListener{

    @Listener
    public void onPacket(PacketReceivedEvent e){
        if(e.getPacket() instanceof ClipboardSendPacket){
            QueuedAsyncTask runnable = new PacketRunnable(e.getTargetEntity(),e.getPacket()) {
                @Override
                public void run() {

                    ClipboardSendPacket packet = (ClipboardSendPacket) getPacket();
                    try {

                        WEGSchematicReader reader = new WEGSchematicReader(new NBTInputStream(new ByteArrayInputStream(packet.getData())));
                        Clipboard clipboard = reader.read(LegacyWorldData.getInstance());
                        ((WEGBlockArrayClipboard) clipboard).setHashCode(packet.getClipboardhash());
                        LocalSession session = WorldEdit.getInstance().getSessionManager().findByName(e.getTargetEntity().getName());
                        if (session == null) {
                            MessageManager.sendMessage(getPlayer(), "clipboard.error.downloading");
                            return;
                        }
                        ClipboardRunnable.setClipboard(getPlayer().getName(), -1);
                        session.setClipboard(new WEGClipboardHolder(clipboard, packet.getClipboardhash()));
                        ClipboardRunnable.setClipboard(getPlayer().getName(), clipboard.hashCode());

                        MessageManager.sendMessage(getPlayer(), "clipboard.finish.downloading", StringUtils.humanReadableByteCount(packet.getData().length, true));


                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            };
            runnable.addToQueue();
        }else if(e.getPacket() instanceof PermissionCheckResponsePacket){
            PermissionManager.getInstance().callPermissionResponse((PermissionCheckResponsePacket) e.getPacket());
        }else if(e.getPacket() instanceof MessageResponsePacket){
            MessageManager.getInstance().callMessageResponse((MessageResponsePacket) e.getPacket());
        }else if(e.getPacket() instanceof ClipboardRequestPacket){
            QueuedAsyncTask runnable = new PacketRunnable(e.getTargetEntity(),e.getPacket()) {
                @Override
                public void run() {
                    ClipboardRequestPacket req = (ClipboardRequestPacket) getPacket();
                    ClipboardSendPacket res = new ClipboardSendPacket();
                    res.setIdentifier(req.getIdentifier());
                    Player p = getPlayer();
                    LocalSession session = WorldEdit.getInstance().getSessionManager().findByName(p.getName());
                    if(session !=null)

                    {


                        try {
                            ClipboardHolder holder = session.getClipboard();
                            if (holder != null) {
                                PacketDataSerializer serializer = new PacketDataSerializer();
                                NBTOutputStream out = new NBTOutputStream(serializer.getBufOut());
                                WEGSchematicWriter writer = new WEGSchematicWriter(out);
                                writer.write(holder.getClipboard(), holder.getWorldData());
                                writer.close();


                                res.setClipboardhash(holder.hashCode());
                                res.setData(serializer.toByteArray());

                                long max = ConfigManager.getInstance().getPluginConfig(p).getMaxClipboardSize();
                                if (max < serializer.toByteArray().length) {
                                    PacketDataSerializer ser = new PacketDataSerializer();
                                    ser.writeLong(serializer.toByteArray().length);
                                    res.setData(ser.toByteArray());
                                    res.setClipboardhash(-3);
                                }
                            }
                        } catch (EmptyClipboardException ex) {

                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    if(res.getData()==null&&res.getClipboardhash()==0)

                    {
                        res.setData(new byte[0]);
                        res.setClipboardhash(-1);
                    }
                    PacketSender.sendPacket(p,res);
                }
            };
           runnable.addToQueue();
        }else if (e.getPacket() instanceof PluginConfigResponsePacket){
            ConfigManager.getInstance().callPLuginConfigResponse((PluginConfigResponsePacket) e.getPacket());
        }else if(e.getPacket() instanceof KeepAlivePacket){
            KeepAlivePacket res = new KeepAlivePacket();
            res.setIdentifier(((KeepAlivePacket) e.getPacket()).getIdentifier());
            PacketSender.sendPacket(e.getTargetEntity(),res);
        }

    }

}
