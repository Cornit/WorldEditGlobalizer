package me.illgilp.worldeditglobalizer.server.bukkit.worldedit.adapter.v1_13_worldedit_7_2_12;

import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Level;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;
import me.illgilp.worldeditglobalizer.preprocessor.adapters.AdapterFilter;
import me.illgilp.worldeditglobalizer.server.bukkit.api.worldedit.adapter.WorldEditAdapter;
import me.illgilp.worldeditglobalizer.server.bukkit.api.worldedit.adapter.WorldEditAdapterFilter;
import me.illgilp.worldeditglobalizer.server.bukkit.api.worldedit.adapter.WorldEditPluginType;
import me.illgilp.worldeditglobalizer.server.bukkit.worldedit.adapter.v1_13_worldedit_7_2_12.schematic.WegBlockArrayClipboard;
import me.illgilp.worldeditglobalizer.server.bukkit.worldedit.adapter.v1_13_worldedit_7_2_12.schematic.WegClipboardHolder;
import me.illgilp.worldeditglobalizer.server.bukkit.worldedit.adapter.v1_13_worldedit_7_2_12.schematic.WegMcEditSchematicReader;
import me.illgilp.worldeditglobalizer.server.bukkit.worldedit.adapter.v1_13_worldedit_7_2_12.schematic.WegSpongeSchematicReader;
import me.illgilp.worldeditglobalizer.server.bukkit.worldedit.adapter.v1_13_worldedit_7_2_12.schematic.WegSpongeSchematicWriter;
import me.illgilp.worldeditglobalizer.server.core.api.WegServer;
import me.illgilp.worldeditglobalizer.server.core.api.clipboard.WegClipboard;
import me.illgilp.worldeditglobalizer.server.core.api.player.WegPlayer;
import org.bukkit.Bukkit;

@AdapterFilter(
    minMcVersion = { 1, 13 },
    wePluginType = WorldEditPluginType.WORLD_EDIT,
    wePluginVersion = { 7, 2, 12 }
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
                                     final WegSpongeSchematicWriter writer = new WegSpongeSchematicWriter(nbtOut)) {
                                    writer.write(clipboard.getClipboard());
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
            if (WegSpongeSchematicReader.isFormat(inputStream)) {
                inputStream = new ByteArrayInputStream(data);
                session.setClipboard(null);
                WegSpongeSchematicReader reader = new WegSpongeSchematicReader(new NBTInputStream(inputStream));
                clipboard = reader.read();
            }
            inputStream = new ByteArrayInputStream(data);
            if (clipboard == null && WegMcEditSchematicReader.isFormat(inputStream)) {
                inputStream = new ByteArrayInputStream(data);
                session.setClipboard(null);
                WegMcEditSchematicReader reader = new WegMcEditSchematicReader(new NBTInputStream(inputStream));
                clipboard = reader.read();
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


}
