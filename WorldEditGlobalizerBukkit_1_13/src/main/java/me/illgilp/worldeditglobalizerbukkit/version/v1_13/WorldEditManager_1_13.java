package me.illgilp.worldeditglobalizerbukkit.version.v1_13;

import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.biome.BiomeType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import me.illgilp.worldeditglobalizerbukkit.WorldEditGlobalizerBukkit;
import me.illgilp.worldeditglobalizerbukkit.clipboard.WEGBlockArrayClipboard;
import me.illgilp.worldeditglobalizerbukkit.manager.MessageManager;
import me.illgilp.worldeditglobalizerbukkit.manager.WorldEditManager;
import me.illgilp.worldeditglobalizerbukkit.runnables.ClipboardRunnable;
import me.illgilp.worldeditglobalizerbukkit.version.v1_13.clipboard.WEGBlockArrayClipboard_1_13;
import me.illgilp.worldeditglobalizerbukkit.version.v1_13.clipboard.WEGClipboardHolder_1_13;
import me.illgilp.worldeditglobalizerbukkit.version.v1_13.clipboard.WEGFAWEBlockArrayClipboard_1_13;
import me.illgilp.worldeditglobalizerbukkit.version.v1_13.clipboard.WEGMcEditSchematicReader_1_13;
import me.illgilp.worldeditglobalizerbukkit.version.v1_13.clipboard.WEGSpongeSchematicReader_1_13;
import me.illgilp.worldeditglobalizerbukkit.version.v1_13.clipboard.WEGSpongeSchematicWriter_1_13;
import me.illgilp.worldeditglobalizerbukkit.version.v1_13.runnable.AWEClipboardRunnable_1_13;
import me.illgilp.worldeditglobalizerbukkit.version.v1_13.runnable.ClipboardRunnable_1_13;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;
import org.bukkit.entity.Player;

public class WorldEditManager_1_13 extends WorldEditManager {

    public WorldEditManager_1_13() {
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
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(new BukkitPlayer(WorldEditGlobalizerBukkit.getInstance().getWorldEditPlugin(), player));
            Clipboard clipboard = null;
            if (WEGSpongeSchematicReader_1_13.isFormat(inputStream)) {
                inputStream = new ByteArrayInputStream(data);
                session.setClipboard(null);
                WEGSpongeSchematicReader_1_13 reader = new WEGSpongeSchematicReader_1_13(new NBTInputStream(inputStream), player.getUniqueId());
                clipboard = reader.read();
            }
            inputStream = new ByteArrayInputStream(data);
            if (clipboard == null && WEGMcEditSchematicReader_1_13.isFormat(inputStream)) {
                inputStream = new ByteArrayInputStream(data);
                session.setClipboard(null);
                WEGMcEditSchematicReader_1_13 reader = new WEGMcEditSchematicReader_1_13(new NBTInputStream(inputStream), player.getUniqueId());
                clipboard = reader.read();
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
            session.setClipboard(new WEGClipboardHolder_1_13(clipboard, hashCode));
            setClipboardHash(player, hashCode);


        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return true;
    }

    @Override
    public ClipboardHolder getClipboardHolder(Player player) {
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(new BukkitPlayer(WorldEditGlobalizerBukkit.getInstance().getWorldEditPlugin(), player));
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
        return WorldEditGlobalizerBukkit.getInstance().isAsyncWorldEdit() ? new AWEClipboardRunnable_1_13(player) : new ClipboardRunnable_1_13(player);
    }

    @Override
    public BlockArrayClipboard createBlockArrayClipboard(Region region, UUID uuid) {
        return WorldEditGlobalizerBukkit.getInstance().isFastAsyncWorldEdit() ? new WEGFAWEBlockArrayClipboard_1_13(region, uuid) : new WEGBlockArrayClipboard_1_13(region);
    }

    @Override
    public byte[] writeClipboard(ClipboardHolder clipboardHolder) {
        PacketDataSerializer serializer = new PacketDataSerializer();

        try {
            NBTOutputStream out = new NBTOutputStream(serializer.getBufOut());
            WEGSpongeSchematicWriter_1_13 writer = new WEGSpongeSchematicWriter_1_13(out);
            writer.write(clipboardHolder.getClipboard());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serializer.toByteArray();
    }

    public static boolean setClipboardBiome(BlockArrayClipboard clipboard, BlockVector3 v3, BiomeType type) {
        try {
            Method method = clipboard.getClass().getDeclaredMethod("setBiome", int.class, int.class, int.class, BiomeType.class);
            return (boolean) method.invoke(clipboard, v3.getBlockX(), v3.getBlockY(), v3.getBlockZ(), type);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {}
        try {
            Method method = clipboard.getClass().getDeclaredMethod("setBiome", BlockVector3.class, BiomeType.class);
            return (boolean) method.invoke(clipboard, v3, type);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static BiomeType getClipboardBiome(Clipboard clipboard, BlockVector3 v3) {
        try {
            Method method = clipboard.getClass().getDeclaredMethod("getBiomeType", int.class, int.class, int.class);
            return (BiomeType) method.invoke(clipboard, v3.getBlockX(), v3.getBlockY(), v3.getBlockZ());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {}
        try {
            Method method = clipboard.getClass().getDeclaredMethod("getBiome", BlockVector3.class);
            return (BiomeType) method.invoke(clipboard, v3);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
