package me.illgilp.worldeditglobalizersponge.runnables;

import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.illgilp.worldeditglobalizersponge.WorldEditGlobalizerSponge;
import me.illgilp.worldeditglobalizersponge.clipboard.WEGSchematicWriter;
import me.illgilp.worldeditglobalizersponge.manager.ConfigManager;
import me.illgilp.worldeditglobalizersponge.manager.MessageManager;
import me.illgilp.worldeditglobalizersponge.manager.PermissionManager;
import me.illgilp.worldeditglobalizersponge.network.PacketSender;
import me.illgilp.worldeditglobalizersponge.network.packets.ClipboardSendPacket;
import me.illgilp.worldeditglobalizersponge.task.AsyncTask;
import me.illgilp.worldeditglobalizersponge.task.QueuedAsyncTask;
import me.illgilp.worldeditglobalizersponge.util.PacketDataSerializer;
import me.illgilp.worldeditglobalizersponge.util.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.util.HashMap;
import java.util.Map;

public class ClipboardRunnable extends AsyncTask {

    private static Map<String,ClipboardRunnable> runnables = new HashMap<>();

    private Player p;

    private int lastHashCode;

    public ClipboardRunnable(Player p) {
        this.p = p;
        runnables.put(p.getName(),this);
    }



    @Override
    public void run() {
        System.out.println("START");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            System.out.println("WHILE");
            try {
                if (p == null) cancel();
                if (!p.isOnline()) cancel();
                System.out.println("UUID: " + p.getUniqueId());
                LocalSession session = WorldEdit.getInstance().getSessionManager().findByName(p.getName());
                if (session == null) continue;
                ClipboardHolder holder = session.getClipboard();
                if (holder != null) {
                    if (lastHashCode == -1) {
                        continue;
                    }
                    if (lastHashCode == holder.hashCode()) {
                        continue;
                    }
                    if (PermissionManager.getInstance().hasPermission(p, "worldeditglobalizer.use.global.clipboard")) {
                        lastHashCode = holder.hashCode();

                        PacketDataSerializer serializer = new PacketDataSerializer();
                        NBTOutputStream out = new NBTOutputStream(serializer.getBufOut());
                        WEGSchematicWriter writer = new WEGSchematicWriter(out);
                        writer.write(holder.getClipboard(), holder.getWorldData());
                        writer.close();
                        long max = ConfigManager.getInstance().getPluginConfig(p).getMaxClipboardSize();
                        if (max < serializer.toByteArray().length) {
                            MessageManager.sendMessage(p, "clipboard.tooBig", StringUtils.humanReadableByteCount(max, true), StringUtils.humanReadableByteCount(serializer.toByteArray().length, true));
                            continue;
                        }
                        ClipboardSendPacket packet = new ClipboardSendPacket();
                        packet.setClipboardhash(holder.hashCode());
                        packet.setData(serializer.toByteArray());

                        MessageManager.sendMessage(p, "clipboard.start.uploading");
                        PacketSender.sendPacket(p, packet);
                    }
                }
            } catch (Exception e) {
                if (e instanceof EmptyClipboardException) continue;
                System.out.println(ExceptionUtils.getStackTrace(e));
            }
        }
    }


    public void cancel() throws IllegalStateException {
        interrupt();
        runnables.remove(p.getName());
    }

    public static void setClipboard(String playerName, int hascode) {
        if(runnables.containsKey(playerName)){
            runnables.get(playerName).lastHashCode = hascode;
        }
    }
}
