package me.illgilp.worldeditglobalizerbukkit.version.v1_8;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.registry.LegacyWorldData;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import me.illgilp.worldeditglobalizerbukkit.WorldEditGlobalizerBukkit;
import me.illgilp.worldeditglobalizerbukkit.clipboard.WEGBlockArrayClipboard;
import me.illgilp.worldeditglobalizerbukkit.manager.MessageManager;
import me.illgilp.worldeditglobalizerbukkit.manager.WorldEditManager;
import me.illgilp.worldeditglobalizerbukkit.runnables.ClipboardRunnable;
import me.illgilp.worldeditglobalizerbukkit.version.v1_8.clipboard.WEGBlockArrayClipboard_1_8;
import me.illgilp.worldeditglobalizerbukkit.version.v1_8.clipboard.WEGClipboardHolder_1_8;
import me.illgilp.worldeditglobalizerbukkit.version.v1_8.clipboard.WEGFAWEBlockArrayClipboard_1_8;
import me.illgilp.worldeditglobalizerbukkit.version.v1_8.clipboard.WEGSchematicReader_1_8;
import me.illgilp.worldeditglobalizerbukkit.version.v1_8.clipboard.WEGSchematicWriter_1_8;
import me.illgilp.worldeditglobalizerbukkit.version.v1_8.runnable.AWEClipboardRunnable_1_8;
import me.illgilp.worldeditglobalizerbukkit.version.v1_8.runnable.ClipboardRunnable_1_8;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;
import org.bukkit.entity.Player;

public class WorldEditManager_1_8 extends WorldEditManager {

    public WorldEditManager_1_8() {
        if (WorldEditGlobalizerBukkit.getInstance().isFastAsyncWorldEdit()) {
            try {
                Class settingsClass = Class.forName("com.boydti.fawe.config.Settings");
                Field impField = settingsClass.getDeclaredField("IMP");
                impField.setAccessible(true);
                Field clipboardField = impField.getType().getDeclaredField("CLIPBOARD");
                clipboardField.setAccessible(true);
                Field useDiskField = clipboardField.getType().getDeclaredField("USE_DISK");
                useDiskField.setAccessible(true);
                useDiskField.setBoolean(clipboardField.get(impField.get(null)), false);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public boolean readAndSetClipboard(Player player, byte[] data, int hashCode) {
        try {
            InputStream inputStream = new ByteArrayInputStream(data);
            Clipboard clipboard = null;
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(new BukkitPlayer(WorldEditGlobalizerBukkit.getInstance().getWorldEditPlugin(), WorldEdit.getInstance().getServer(),player));
            if (isNewFormat(inputStream)) {
                inputStream = new ByteArrayInputStream(data);
                MessageManager.sendMessage(player, "clipboard.tooNew");
                return false;
            }
            inputStream = new ByteArrayInputStream(data);
            if (clipboard == null && WEGSchematicReader_1_8.isFormat(inputStream)) {
                inputStream = new ByteArrayInputStream(data);
                session.setClipboard(null);
                WEGSchematicReader_1_8 reader = new WEGSchematicReader_1_8(new NBTInputStream(inputStream), player.getUniqueId());
                clipboard = reader.read(LegacyWorldData.getInstance());
            }
            if (clipboard == null) {
                MessageManager.sendMessage(player, "clipboard.unknownFormat");
                return false;
            }
            ((WEGBlockArrayClipboard) clipboard).setHashCode(hashCode);

            if (session == null) {
                MessageManager.sendMessage(player, "clipboard.error.downloading");
                return false;
            }
            setClipboardHash(player, -1);
            session.setClipboard(new WEGClipboardHolder_1_8(clipboard, hashCode));
            setClipboardHash(player, hashCode);

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return true;
    }

    @Override
    public ClipboardHolder getClipboardHolder(Player player) {
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(new BukkitPlayer(WorldEditGlobalizerBukkit.getInstance().getWorldEditPlugin(), WorldEdit.getInstance().getServer(),player));
        if (session != null) {
            try {
                return session.getClipboard();
            } catch (EmptyClipboardException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    protected ClipboardRunnable getRunnable(Player player) {
        return WorldEditGlobalizerBukkit.getInstance().isAsyncWorldEdit() ? new AWEClipboardRunnable_1_8(player) : new ClipboardRunnable_1_8(player);
    }

    @Override
    public byte[] writeClipboard(ClipboardHolder clipboardHolder) {
        PacketDataSerializer serializer = new PacketDataSerializer();

        try {
            NBTOutputStream out = new NBTOutputStream(serializer.getBufOut());
            WEGSchematicWriter_1_8 writer = new WEGSchematicWriter_1_8(out);
            writer.write(clipboardHolder.getClipboard(), LegacyWorldData.getInstance());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serializer.toByteArray();
    }

    @Override
    public BlockArrayClipboard createBlockArrayClipboard(Region region, UUID uuid) {
        return WorldEditGlobalizerBukkit.getInstance().isFastAsyncWorldEdit() ? new WEGFAWEBlockArrayClipboard_1_8(region, uuid) : new WEGBlockArrayClipboard_1_8(region);
    }

    private boolean isNewFormat(InputStream inputStream) {
        try (NBTInputStream str = new NBTInputStream(inputStream)) {
            NamedTag rootTag = str.readNamedTag();
            if (!rootTag.getName().equals("Schematic")) {
                return false;
            }
            CompoundTag schematicTag = (CompoundTag) rootTag.getTag();

            // Check
            Map<String, Tag> schematic = schematicTag.getValue();
            if (!schematic.containsKey("Version")) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

}
