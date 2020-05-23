package me.illgilp.worldeditglobalizerbukkit.version.v1_13.runnable;

import com.sk89q.worldedit.session.ClipboardHolder;
import java.io.IOException;
import me.illgilp.worldeditglobalizerbukkit.WorldEditGlobalizerBukkit;
import me.illgilp.worldeditglobalizerbukkit.manager.ConfigManager;
import me.illgilp.worldeditglobalizerbukkit.manager.MessageManager;
import me.illgilp.worldeditglobalizerbukkit.manager.PermissionManager;
import me.illgilp.worldeditglobalizerbukkit.manager.VersionManager;
import me.illgilp.worldeditglobalizerbukkit.network.PacketSender;
import me.illgilp.worldeditglobalizerbukkit.runnables.ClipboardRunnable;
import me.illgilp.worldeditglobalizerbukkit.util.StringUtils;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;
import me.illgilp.worldeditglobalizercommon.network.packets.ClipboardSendPacket;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacerPlayer;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;

public class AWEClipboardRunnable_1_13 extends ClipboardRunnable {


    private Player p;

    private int lastHashCode;

    public AWEClipboardRunnable_1_13(Player p) {
        this.p = p;
    }

    @Override
    public void setClipboardHash(int clipboardHash) {
        this.lastHashCode = clipboardHash;
    }

    @Override
    public void run() {
        try {
            if (p == null) cancel();
            if (!p.isOnline()) cancel();
            ClipboardHolder holder = VersionManager.getInstance().getWorldEditManager().getClipboardHolder(p);

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
            WorldEditGlobalizerBukkit.getInstance().getLogger().warning(ExceptionUtils.getFullStackTrace(e));
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();
    }

    @Override
    public Player getPlayer() {
        return p;
    }

    private void sendClipboard(ClipboardHolder holder) throws IOException {
        if (!ConfigManager.getInstance().getPluginConfig(p).isEnableClipboardAutoUpload()) {
            return;
        }
        if (PermissionManager.getInstance().hasPermission(p, "worldeditglobalizer.use.global.clipboard")) {
            lastHashCode = holder.hashCode();

            PacketDataSerializer serializer = new PacketDataSerializer();
            serializer.writeByteArray(VersionManager.getInstance().getWorldEditManager().writeClipboard(holder));

            long max = ConfigManager.getInstance().getPluginConfig(p).getMaxClipboardSize();
            if (max < serializer.toByteArray().length) {
                MessageManager.sendMessage(p, "clipboard.tooBig", StringUtils.humanReadableByteCount(max, true), StringUtils.humanReadableByteCount(serializer.toByteArray().length, true));
                return;
            }

            ClipboardSendPacket packet = new ClipboardSendPacket();
            packet.setClipboardHash(holder.hashCode());
            packet.setData(serializer.toByteArray());
            packet.setAction(ClipboardSendPacket.Action.SEND);

            MessageManager.sendMessage(p, "clipboard.start.uploading");
            PacketSender.sendPacket(p, packet);
        }
    }
}
