package me.illgilp.worldeditglobalizer.proxy.core.cache;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.illgilp.worldeditglobalizer.common.network.data.stream.PacketDataInputStream;
import me.illgilp.worldeditglobalizer.common.network.data.stream.PacketDataOutputStream;
import me.illgilp.worldeditglobalizer.proxy.core.api.WegProxy;
import me.illgilp.worldeditglobalizer.proxy.core.api.clipboard.WegClipboardContainer;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegOfflinePlayer;
import me.illgilp.worldeditglobalizer.proxy.core.api.schematic.WegSchematicContainer;
import me.illgilp.worldeditglobalizer.proxy.core.player.WegCoreOfflinePlayer;

@RequiredArgsConstructor
public class OfflinePlayerCache {

    private final Map<UUID, WegCoreOfflinePlayerImpl> offlinePlayersById = new TreeMap<>();
    private final Map<String, WegCoreOfflinePlayerImpl> offlinePlayersByName = new TreeMap<>();

    private final WegSchematicContainer schematicContainer;

    public Optional<WegOfflinePlayer> getPlayer(UUID uuid) {
        return Optional.ofNullable(offlinePlayersById.get(uuid));
    }

    public Optional<WegOfflinePlayer> getPlayer(String name) {
        return Optional.ofNullable(offlinePlayersByName.get(name));
    }

    public void updatePlayer(WegOfflinePlayer player) throws IOException {
        Optional<WegCoreOfflinePlayerImpl> pl = Optional.ofNullable(offlinePlayersById.get(player.getUniqueId()));
        if (!pl.isPresent()) {
            WegCoreOfflinePlayerImpl pp = new WegCoreOfflinePlayerImpl(new OfflinePlayerCacheModel(
                player.getUniqueId(),
                player.getName(),
                Instant.now().plus(30, ChronoUnit.DAYS)
            ));
            offlinePlayersById.put(pp.getUniqueId(), pp);
            offlinePlayersByName.put(pp.getName(), pp);
            save();
            return;
        }
        pl.map(WegCoreOfflinePlayerImpl::getName).ifPresent(offlinePlayersByName::remove);
        pl.map(WegCoreOfflinePlayerImpl::getModel).ifPresent(model -> {
            model.setName(player.getName());
            model.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        });
        pl.ifPresent(pla -> offlinePlayersByName.put(pla.getName(), pla));
        save();
    }

    public List<WegOfflinePlayer> getByNameStartingWith(String start) {
        String finalStart = start.toLowerCase();
        return offlinePlayersById.values().stream().filter(m -> m.getName().toLowerCase().startsWith(finalStart)).collect(Collectors.toList());
    }

    public void save() throws IOException {
        try (
            PacketDataOutputStream out = new PacketDataOutputStream(
                Files.newOutputStream(new File(WegProxy.getInstance().getDataFolder(), "usercache.dat").toPath()))
        ) {
            final List<WegCoreOfflinePlayerImpl> players = new ArrayList<>(offlinePlayersById.values())
                .stream()
                .filter(p -> Instant.now().isBefore(p.model.getExpiresAt()))
                .collect(Collectors.toList());
            out.writeVarInt(players.size());
            for (WegCoreOfflinePlayerImpl player : players) {
                player.model.write(out);
            }
            out.flush();
        }
    }

    public void load() throws IOException {
        this.offlinePlayersById.clear();
        this.offlinePlayersByName.clear();
        File file = new File(WegProxy.getInstance().getDataFolder(), "usercache.dat");
        if (file.exists()) {
            try (PacketDataInputStream in = new PacketDataInputStream(Files.newInputStream(file.toPath()))) {
                int size = in.readVarInt();
                for (int i = 0; i < size; i++) {
                    OfflinePlayerCacheModel model = new OfflinePlayerCacheModel();
                    model.read(in);
                    if (Instant.now().isAfter(model.getExpiresAt())) {
                        continue;
                    }
                    WegCoreOfflinePlayerImpl pl = new WegCoreOfflinePlayerImpl(model);
                    offlinePlayersById.put(pl.getUniqueId(), pl);
                    offlinePlayersByName.put(pl.getName(), pl);
                }
            }
        }
    }

    public void cleanup() {
        final List<WegCoreOfflinePlayerImpl> players = new ArrayList<>(offlinePlayersById.values());
        for (WegCoreOfflinePlayerImpl player : players) {
            if (Instant.now().isAfter(player.model.getExpiresAt())) {
                offlinePlayersById.remove(player.getUniqueId());
                offlinePlayersByName.remove(player.getName());
            }
        }
    }


    @RequiredArgsConstructor
    private class WegCoreOfflinePlayerImpl extends WegCoreOfflinePlayer {

        private final OfflinePlayerCacheModel model;
        private WegClipboardContainer clipboardContainer;

        @Override
        public UUID getUniqueId() {
            return model.getUuid();
        }

        @Override
        public String getName() {
            return model.getName();
        }

        @Override
        public boolean isOnline() {
            return false;
        }

        @Override
        public WegClipboardContainer getClipboardContainer() {
            return clipboardContainer == null ? (clipboardContainer = new WegClipboardContainer(this.getUniqueId())) : clipboardContainer;
        }

        @Override
        public WegSchematicContainer getSchematicContainer() {
            return OfflinePlayerCache.this.schematicContainer;
        }

        public OfflinePlayerCacheModel getModel() {
            return model;
        }

        @Override
        public String toString() {
            return "WegCoreOfflinePlayerImpl{" +
                "model=" + model +
                '}';
        }
    }

}
