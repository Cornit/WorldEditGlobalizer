package me.illgilp.worldeditglobalizer.server.bukkit.worldedit.adapter.v1_8_fawe_22_3_9;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.world.registry.LegacyWorldData;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;
import me.illgilp.worldeditglobalizer.preprocessor.adapters.AdapterFilter;
import me.illgilp.worldeditglobalizer.server.bukkit.api.worldedit.adapter.WorldEditAdapter;
import me.illgilp.worldeditglobalizer.server.bukkit.api.worldedit.adapter.WorldEditAdapterFilter;
import me.illgilp.worldeditglobalizer.server.bukkit.api.worldedit.adapter.WorldEditPluginType;
import me.illgilp.worldeditglobalizer.server.bukkit.worldedit.adapter.v1_8_fawe_22_3_9.schematic.WegBlockArrayClipboard;
import me.illgilp.worldeditglobalizer.server.bukkit.worldedit.adapter.v1_8_fawe_22_3_9.schematic.WegClipboardHolder;
import me.illgilp.worldeditglobalizer.server.bukkit.worldedit.adapter.v1_8_fawe_22_3_9.schematic.WegSchematicReader;
import me.illgilp.worldeditglobalizer.server.bukkit.worldedit.adapter.v1_8_fawe_22_3_9.schematic.WegSchematicWriter;
import me.illgilp.worldeditglobalizer.server.core.api.WegServer;
import me.illgilp.worldeditglobalizer.server.core.api.clipboard.WegClipboard;
import me.illgilp.worldeditglobalizer.server.core.api.player.WegPlayer;
import org.bukkit.Bukkit;

@AdapterFilter(
    minMcVersion = { 1, 8 },
    wePluginType = WorldEditPluginType.FAST_ASYNC_WORLD_EDIT,
    wePluginVersion = { 22, 3, 9 }
)
public class WorldEditAdapterImpl extends WorldEditAdapter {
    private final WorldEditPlugin worldEditPlugin;

    public WorldEditAdapterImpl(WorldEditAdapterFilter filter) {
        super(filter);
        this.worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
    }

    @Override
    public Optional<WegClipboard> getClipboardOfPlayer(WegPlayer player) {
        return Optional.ofNullable(Bukkit.getPlayer(player.getUniqueId()))
            .map(worldEditPlugin::getSession)
            .flatMap(localSession -> {
                try {
                    return Optional.ofNullable(localSession.getClipboard())
                        .map(clipboard -> new WegClipboard() {
                            @Override
                            public int getHash() {
                                return clipboard.hashCode();
                            }

                            @Override
                            public byte[] write() throws IOException {
                                try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
                                     final NBTOutputStream nbtOut = new NBTOutputStream(out);
                                     final WegSchematicWriter writer = new WegSchematicWriter(nbtOut)) {
                                    writer.write(clipboard.getClipboard(), LegacyWorldData.getInstance());
                                    return out.toByteArray();
                                }
                            }

                            @Override
                            public boolean isWegClipboard() {
                                return clipboard instanceof WegClipboardHolder;
                            }
                        });
                } catch (EmptyClipboardException e) {
                    return Optional.empty();
                }
            });
    }

    @Override
    public boolean readAndSetClipboard(WegPlayer player, byte[] data, int hashCode) {
        try {
            InputStream inputStream = new ByteArrayInputStream(data);
            Optional<LocalSession> sessionOptional = Optional.ofNullable(Bukkit.getPlayer(player.getUniqueId()))
                .map(worldEditPlugin::getSession);
            Clipboard clipboard = null;
            if (!sessionOptional.isPresent()) {
                MessageHelper.builder()
                    .translation(TranslationKey.CLIPBOARD_DOWNLOAD_ERROR)
                    .sendMessageTo(player);
                WegServer.getInstance().getLogger()
                    .severe("No WorldEdit session found for player '" + player.getName() + "'");
                return false;
            }
            LocalSession session = sessionOptional.get();
            if (isNewFormat(inputStream)) {
                MessageHelper.builder()
                    .translation(TranslationKey.CLIPBOARD_DOWNLOAD_TOO_NEW)
                    .sendMessageTo(player);
                return false;
            }
            inputStream = new ByteArrayInputStream(data);
            if (WegSchematicReader.isFormat(inputStream)) {
                inputStream = new ByteArrayInputStream(data);
                session.setClipboard(null);
                WegSchematicReader reader = new WegSchematicReader(new NBTInputStream(inputStream));
                clipboard = reader.read(LegacyWorldData.getInstance());
            }
            if (clipboard == null) {
                MessageHelper.builder()
                    .translation(TranslationKey.CLIPBOARD_DOWNLOAD_ERROR)
                    .sendMessageTo(player);
                WegServer.getInstance().getLogger()
                    .severe("Found unknown clipboard format for downloaded clipboard of player '" + player.getName() + "'");
                return false;
            }
            ((WegBlockArrayClipboard) clipboard).setHashCode(hashCode);
            session.setClipboard(new WegClipboardHolder(clipboard, hashCode));
        } catch (IOException e1) {
            MessageHelper.builder()
                .translation(TranslationKey.CLIPBOARD_DOWNLOAD_ERROR)
                .sendMessageTo(player);
            WegServer.getInstance().getLogger()
                .log(Level.SEVERE, "Exception while reading and setting clipboard of player '" + player.getName() + "'", e1);
        }
        return true;
    }

    private boolean isNewFormat(InputStream inputStream) {
        try (NBTInputStream str = new NBTInputStream(inputStream)) {
            NamedTag rootTag = str.readNamedTag();
            if (!rootTag.getName().equals("Schematic")) {
                return false;
            }
            CompoundTag schematicTag = (CompoundTag) rootTag.getTag();
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
