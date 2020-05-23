package me.illgilp.worldeditglobalizerbukkit.listener;

import com.sk89q.worldedit.session.ClipboardHolder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import me.illgilp.worldeditglobalizerbukkit.WorldEditGlobalizerBukkit;
import me.illgilp.worldeditglobalizerbukkit.events.PacketReceivedEvent;
import me.illgilp.worldeditglobalizerbukkit.manager.ConfigManager;
import me.illgilp.worldeditglobalizerbukkit.manager.MessageManager;
import me.illgilp.worldeditglobalizerbukkit.manager.PermissionManager;
import me.illgilp.worldeditglobalizerbukkit.manager.VersionManager;
import me.illgilp.worldeditglobalizerbukkit.network.PacketSender;
import me.illgilp.worldeditglobalizerbukkit.runnables.PacketRunnable;
import me.illgilp.worldeditglobalizerbukkit.util.StringUtils;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;
import me.illgilp.worldeditglobalizercommon.network.packets.ClipboardRequestPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.ClipboardSendPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.KeepAlivePacket;
import me.illgilp.worldeditglobalizercommon.network.packets.MessageResponsePacket;
import me.illgilp.worldeditglobalizercommon.network.packets.PermissionCheckResponsePacket;
import me.illgilp.worldeditglobalizercommon.network.packets.PluginConfigResponsePacket;
import me.illgilp.worldeditglobalizercommon.network.packets.PluginSendPacket;
import me.illgilp.worldeditglobalizercommon.network.packets.PluginSendResultPacket;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PacketReceivedListener implements Listener {

    @EventHandler
    public void onPacket(PacketReceivedEvent e) {
        WorldEditGlobalizerBukkit.getInstance().getConfigManager().registerConfig(WorldEditGlobalizerBukkit.getInstance().getMainConfig());
        if (e.getPacket() instanceof ClipboardSendPacket) {
            new PacketRunnable(e.getPlayer(), e.getPacket()) {
                @Override
                public void run() {
                    ClipboardSendPacket packet = (ClipboardSendPacket) getPacket();
                    if (VersionManager.getInstance().getWorldEditManager().readAndSetClipboard(getPlayer(), packet.getData(), packet.hashCode())) {
                        MessageManager.sendMessage(getPlayer(), "clipboard.finish.downloading", StringUtils.humanReadableByteCount(packet.getData().length, true));
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
                    ClipboardHolder holder = VersionManager.getInstance().getWorldEditManager().getClipboardHolder(p);
                    if (holder != null) {
                        res.setClipboardHash(holder.hashCode());
                        res.setData(VersionManager.getInstance().getWorldEditManager().writeClipboard(holder));

                        long max = ConfigManager.getInstance().getPluginConfig(p).getMaxClipboardSize();
                        if (max < res.getData().length) {
                            PacketDataSerializer ser = new PacketDataSerializer();
                            ser.writeLong(res.getData().length);
                            res.setData(ser.toByteArray());
                            res.setClipboardHash(-3);
                            res.setAction(ClipboardSendPacket.Action.TOO_BIG);
                        }
                    }
                    if (res.getData() == null && res.getClipboardHash() == 0) {
                        res.setData(new byte[0]);
                        res.setClipboardHash(-1);
                        res.setAction(ClipboardSendPacket.Action.CLEAR);
                    }
                    PacketSender.sendPacket(p, res);
                }
            }.runTaskAsynchronously(WorldEditGlobalizerBukkit.getInstance());
        } else if (e.getPacket() instanceof PluginConfigResponsePacket) {
            ConfigManager.getInstance().callPluginConfigResponse((PluginConfigResponsePacket) e.getPacket());
        } else if (e.getPacket() instanceof KeepAlivePacket) {
            KeepAlivePacket res = new KeepAlivePacket(WorldEditGlobalizerBukkit.getInstance().getDescription().getVersion());
            if (!((KeepAlivePacket) e.getPacket()).getVersion().equals(WorldEditGlobalizerBukkit.getInstance().getDescription().getVersion())) {
                WorldEditGlobalizerBukkit.getInstance().setUsable(false);
            }
            res.setIdentifier(((KeepAlivePacket) e.getPacket()).getIdentifier());
            PacketSender.sendPacket(e.getPlayer(), res);
        } else if (e.getPacket() instanceof PluginSendPacket) {

            try {
                File folder = new File("plugins/update");
                if (!folder.exists()) folder.mkdirs();
                File file = new File(folder,WorldEditGlobalizerBukkit.getInstance().getFile().getName());
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                IOUtils.write(((PluginSendPacket) e.getPacket()).getData(), fileOutputStream);
                fileOutputStream.close();
                try {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    byte[] data = IOUtils.toByteArray(fileInputStream);
                    fileInputStream.close();
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] hash = digest.digest(data);
                    PluginSendResultPacket.Result result = PluginSendResultPacket.Result.FAILED;
                    if (Arrays.equals(hash, ((PluginSendPacket) e.getPacket()).getHash())) {
                        result = PluginSendResultPacket.Result.SUCCESS;
                    }
                    PluginSendResultPacket pluginSendResultPacket = new PluginSendResultPacket();
                    pluginSendResultPacket.setIdentifier(((PluginSendPacket) e.getPacket()).getIdentifier());
                    pluginSendResultPacket.setResult(result);
                    pluginSendResultPacket.setTryNum(((PluginSendPacket) e.getPacket()).getTryNum());
                    PacketSender.sendPacket(e.getPlayer(), pluginSendResultPacket);
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (NoSuchAlgorithmException ex) {
                    ex.printStackTrace();
                }
            } catch (IOException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }

//            KeepAlivePacket res = new KeepAlivePacket(WorldEditGlobalizerBukkit.getInstance().getDescription().getVersion());
//            res.setIdentifier(((KeepAlivePacket) e.getPacket()).getIdentifier());
//            PacketSender.sendPacket(e.getPlayer(), res);
        }

    }

}
