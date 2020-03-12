package me.illgilp.worldeditglobalizerbukkit.runnables;

import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.session.ClipboardHolder;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import me.illgilp.worldeditglobalizerbukkit.WorldEditGlobalizerBukkit;
import me.illgilp.worldeditglobalizerbukkit.clipboard.WEGSpongeSchematicWriter;
import me.illgilp.worldeditglobalizerbukkit.manager.ConfigManager;
import me.illgilp.worldeditglobalizerbukkit.manager.MessageManager;
import me.illgilp.worldeditglobalizerbukkit.manager.PermissionManager;
import me.illgilp.worldeditglobalizerbukkit.network.PacketSender;
import me.illgilp.worldeditglobalizerbukkit.util.StringUtils;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;
import me.illgilp.worldeditglobalizercommon.network.packets.ClipboardSendPacket;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacerPlayer;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;

public class AWEClipboardRunnable extends BukkitRunnable {

    private static Map<String, AWEClipboardRunnable> runnables = new HashMap<>();

    private Player p;

    private int lastHashCode;

    public AWEClipboardRunnable(Player p) {
        this.p = p;
        runnables.put(p.getName(), this);
    }

    public static void stop(String player) {
        if (runnables.containsKey(player)) {
            runnables.get(player).cancel();
        }
    }

    public static void setClipboard(String playerName, int hascode) {
        if (runnables.containsKey(playerName)) {
            runnables.get(playerName).lastHashCode = hascode;
        }
    }

    @Override
    public void run() {
        try {
            if (p == null) cancel();
            if (!p.isOnline()) cancel();
            LocalSession session = WorldEdit.getInstance().getSessionManager().findByName(p.getName());
            if (session == null) return;
            ClipboardHolder holder = session.getClipboard();

            if (holder != null) {
                if (lastHashCode == -1) {
                    return;
                }
                if (lastHashCode == holder.hashCode()) {
                    return;
                }
                IAsyncWorldEdit awe = (IAsyncWorldEdit) Bukkit.getPluginManager().getPlugin("AsyncWorldEdit");
                IPlayerEntry iplayer = awe.getPlayerManager().getPlayer(p.getUniqueId());
                if (iplayer != null) {
                    IBlockPlacerPlayer placer = awe.getBlockPlacer().getPlayerEvents(iplayer);
                    if (placer != null) {
                        if (!placer.hasJobs()) {
                            sendClipboard(holder);
                        }
                    } else {
                        sendClipboard(holder);
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof EmptyClipboardException) return;
            WorldEditGlobalizerBukkit.getInstance().getLogger().warning(ExceptionUtils.getFullStackTrace(e));
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        runnables.remove(p.getName());
    }

    private void sendClipboard(ClipboardHolder holder) throws IOException {
        if (!ConfigManager.getInstance().getPluginConfig(p).isEnableClipboardAutoUpload()) {
            return;
        }
        if (PermissionManager.getInstance().hasPermission(p, "worldeditglobalizer.use.global.clipboard")) {
            lastHashCode = holder.hashCode();

            PacketDataSerializer serializer = new PacketDataSerializer();
            NBTOutputStream out = new NBTOutputStream(serializer.getBufOut());
            WEGSpongeSchematicWriter writer = new WEGSpongeSchematicWriter(out);
            writer.write(holder.getClipboard());
            writer.close();

            long max = ConfigManager.getInstance().getPluginConfig(p).getMaxClipboardSize();
            if (max < serializer.toByteArray().length) {
                MessageManager.sendMessage(p, "clipboard.tooBig", StringUtils.humanReadableByteCount(max, true), StringUtils.humanReadableByteCount(serializer.toByteArray().length, true));
                return;
            }

            ClipboardSendPacket packet = new ClipboardSendPacket();
            packet.setClipboardHash(holder.hashCode());
            packet.setData(serializer.toByteArray());

            MessageManager.sendMessage(p, "clipboard.start.uploading");
            PacketSender.sendPacket(p, packet);
        }
    }
}
