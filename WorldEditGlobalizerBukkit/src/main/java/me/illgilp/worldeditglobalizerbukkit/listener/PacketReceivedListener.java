package me.illgilp.worldeditglobalizerbukkit.listener;

import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.illgilp.worldeditglobalizerbukkit.WorldEditGlobalizerBukkit;
import me.illgilp.worldeditglobalizerbukkit.clipboard.*;
import me.illgilp.worldeditglobalizerbukkit.events.PacketReceivedEvent;
import me.illgilp.worldeditglobalizerbukkit.manager.ConfigManager;
import me.illgilp.worldeditglobalizerbukkit.manager.MessageManager;
import me.illgilp.worldeditglobalizerbukkit.manager.PermissionManager;
import me.illgilp.worldeditglobalizerbukkit.network.PacketSender;
import me.illgilp.worldeditglobalizerbukkit.runnables.ClipboardRunnable;
import me.illgilp.worldeditglobalizerbukkit.runnables.PacketRunnable;
import me.illgilp.worldeditglobalizerbukkit.util.StringUtils;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;
import me.illgilp.worldeditglobalizercommon.network.packets.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class PacketReceivedListener implements Listener {

    @EventHandler
    public void onPacket(PacketReceivedEvent e) {
        if (e.getPacket() instanceof ClipboardSendPacket) {
            new PacketRunnable(e.getPlayer(), e.getPacket()) {
                @Override
                public void run() {

                    ClipboardSendPacket packet = (ClipboardSendPacket) getPacket();
                    try {
                        InputStream inputStream = new ByteArrayInputStream(packet.getData());
                        Clipboard clipboard = null;
                        if (WEGSpongeSchematicReader.isFormat(inputStream)) {
                            inputStream = new ByteArrayInputStream(packet.getData());
                            WEGSpongeSchematicReader reader = new WEGSpongeSchematicReader(new NBTInputStream(inputStream));
                            clipboard = reader.read();
                        }
                        inputStream = new ByteArrayInputStream(packet.getData());
                        if (clipboard == null && WEGMcEditSchematicReader.isFormat(inputStream)) {
                            inputStream = new ByteArrayInputStream(packet.getData());
                            WEGMcEditSchematicReader reader = new WEGMcEditSchematicReader(new NBTInputStream(inputStream));
                            clipboard = reader.read();
                        }
                        if (clipboard == null) {
                            MessageManager.sendMessage(getPlayer(), "clipboard.unknownFormat");
                            return;
                        }
                        ((WEGBlockArrayClipboard) clipboard).setHashCode(packet.getClipboardhash());
                        LocalSession session = WorldEdit.getInstance().getSessionManager().get(new BukkitPlayer(WorldEditGlobalizerBukkit.getInstance().getWorldEditPlugin(), getPlayer()));
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
            }.runTaskAsynchronously(WorldEditGlobalizerBukkit.getInstance());
        } else if (e.getPacket() instanceof PermissionCheckResponsePacket) {
            PermissionManager.getInstance().callPermissionResponse((PermissionCheckResponsePacket) e.getPacket());
        } else if (e.getPacket() instanceof MessageResponsePacket) {
            MessageManager.getInstance().callMessageResponse((MessageResponsePacket) e.getPacket());
        } else if (e.getPacket() instanceof ClipboardRequestPacket) {
            new PacketRunnable(e.getPlayer(), e.getPacket()) {
                @Override
                public void run() {
                    ClipboardRequestPacket req = (ClipboardRequestPacket) getPacket();
                    ClipboardSendPacket res = new ClipboardSendPacket();
                    res.setIdentifier(req.getIdentifier());
                    org.bukkit.entity.Player p = getPlayer();
                    LocalSession session = WorldEdit.getInstance().getSessionManager().findByName(p.getName());
                    if (session != null) {


                        try {
                            ClipboardHolder holder = session.getClipboard();
                            if (holder != null) {
                                PacketDataSerializer serializer = new PacketDataSerializer();
                                NBTOutputStream out = new NBTOutputStream(serializer.getBufOut());
                                WEGSpongeSchematicWriter writer = new WEGSpongeSchematicWriter(out);
                                writer.write(holder.getClipboard());
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
                    if (res.getData() == null && res.getClipboardhash() == 0) {
                        res.setData(new byte[0]);
                        res.setClipboardhash(-1);
                    }
                    PacketSender.sendPacket(p, res);
                }
            }.runTaskAsynchronously(WorldEditGlobalizerBukkit.getInstance());
        } else if (e.getPacket() instanceof PluginConfigResponsePacket) {
            ConfigManager.getInstance().callPLuginConfigResponse((PluginConfigResponsePacket) e.getPacket());
        } else if (e.getPacket() instanceof KeepAlivePacket) {
            KeepAlivePacket res = new KeepAlivePacket(WorldEditGlobalizerBukkit.getInstance().getDescription().getVersion());
            res.setIdentifier(((KeepAlivePacket) e.getPacket()).getIdentifier());
            PacketSender.sendPacket(e.getPlayer(), res);
        }

    }

}
