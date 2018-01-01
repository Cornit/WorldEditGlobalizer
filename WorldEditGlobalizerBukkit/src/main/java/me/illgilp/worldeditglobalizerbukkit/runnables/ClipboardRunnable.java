package me.illgilp.worldeditglobalizerbukkit.runnables;

import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.illgilp.worldeditglobalizerbukkit.WorldEditGlobalizerBukkit;
import me.illgilp.worldeditglobalizerbukkit.clipboard.WEGSchematicWriter;
import me.illgilp.worldeditglobalizerbukkit.manager.ConfigManager;
import me.illgilp.worldeditglobalizerbukkit.manager.MessageManager;
import me.illgilp.worldeditglobalizerbukkit.manager.PermissionManager;
import me.illgilp.worldeditglobalizerbukkit.network.PacketSender;
import me.illgilp.worldeditglobalizerbukkit.network.packets.ClipboardSendPacket;
import me.illgilp.worldeditglobalizerbukkit.util.PacketDataSerializer;
import me.illgilp.worldeditglobalizerbukkit.util.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ClipboardRunnable extends BukkitRunnable {

    private static Map<String,ClipboardRunnable> runnables = new HashMap<>();

    private Player p;

    private int lastHashCode;

    public ClipboardRunnable(Player p) {
        this.p = p;
        runnables.put(p.getName(),this);
    }

    @Override
    public void run() {
        try {
            if(p == null)cancel();
            if(!p.isOnline())cancel();
            LocalSession session = WorldEdit.getInstance().getSessionManager().findByName(p.getName());
            if(session == null)return;
            ClipboardHolder holder = session.getClipboard();
            if(holder != null) {
                if (lastHashCode == -1) {
                    return;
                }
                if(lastHashCode == holder.hashCode()){
                    return;
                }
                if (PermissionManager.getInstance().hasPermission(p, "worldeditglobalizer.use.global.clipboard")) {
                    lastHashCode = holder.hashCode();

                    PacketDataSerializer serializer = new PacketDataSerializer();
                    NBTOutputStream out = new NBTOutputStream(serializer.getBufOut());
                    WEGSchematicWriter writer = new WEGSchematicWriter(out);
                    writer.write(holder.getClipboard(), holder.getWorldData());
                    writer.close();

                    long max = ConfigManager.getInstance().getPluginConfig(p).getMaxClipboardSize();
                    if(max<serializer.toByteArray().length){
                        MessageManager.sendMessage(p,"clipboard.tooBig", StringUtils.humanReadableByteCount(max,true),StringUtils.humanReadableByteCount(serializer.toByteArray().length,true));
                        return;
                    }

                    ClipboardSendPacket packet = new ClipboardSendPacket();
                    packet.setClipboardhash(holder.hashCode());
                    packet.setData(serializer.toByteArray());

                    MessageManager.sendMessage(p,"clipboard.start.uploading");
                    PacketSender.sendPacket(p,packet);
                }
            }
        } catch (Exception e) {
            if(e instanceof EmptyClipboardException)return;
            WorldEditGlobalizerBukkit.getInstance().getLogger().warning(ExceptionUtils.getFullStackTrace(e));
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        runnables.remove(p.getName());
    }

    public static void setClipboard(String playerName, int hascode) {
        if(runnables.containsKey(playerName)){
            runnables.get(playerName).lastHashCode = hascode;
        }
    }
}