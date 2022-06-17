package me.illgilp.worldeditglobalizerbungee.storage.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;

public class UserCache {

    private static final String FILENAME = "user_cache.dat";

    private final Map<UUID, UserCacheModel> userUuidLookup = new TreeMap<>();
    private final Map<String, UserCacheModel> userNameLookup = new TreeMap<>();

    private File storageFile;

    public UserCache(File storageDirectory) {
        this.storageFile = new File(storageDirectory, FILENAME);
        if (this.storageFile.isFile()) {
            try (InputStream inputStream = Files.newInputStream(this.storageFile.toPath())) {
                PacketDataSerializer s = new PacketDataSerializer(inputStream);
                int size = s.readVarInt();
                for (int i = 0; i < size; i++) {
                    UserCacheModel model = new UserCacheModel();
                    if (model.read(s)) {
                        if ((System.currentTimeMillis() - model.getLastLogin().getTime()) < 1000L * 60 * 60 * 24 * 30) {
                            userUuidLookup.put(model.getUUID(), model);
                            userNameLookup.put(model.getName(), model);
                        }
                    }
                }
            } catch (Exception e) {
                WorldEditGlobalizerBungee.getInstance().getLogger().log(Level.WARNING, "Could not load user_cache.dat -> create new...");
                userUuidLookup.clear();
                userNameLookup.clear();
            }
        }
    }

    public void save() {
        try (OutputStream outputStream = Files.newOutputStream(this.storageFile.toPath())) {
            PacketDataSerializer s = new PacketDataSerializer(outputStream);
            s.writeVarInt(userNameLookup.size());
            for (UserCacheModel model : userUuidLookup.values()) {
                if ((System.currentTimeMillis() - model.getLastLogin().getTime()) < 1000L * 60 * 60 * 24 * 30) {
                    model.write(s);
                }
            }
        } catch (Exception e) {
            WorldEditGlobalizerBungee.getInstance().getLogger().log(Level.WARNING, "Could not save user_cache.dat", e);
        }
    }

    public UserCacheModel getByName(String name) {
        return userNameLookup.get(name.toLowerCase());
    }

    public UserCacheModel getByUUID(UUID uuid) {
        return userUuidLookup.get(uuid);
    }

    public void addToCache(UserCacheModel model) {
        userUuidLookup.put(model.getUUID(), model);
        userNameLookup.put(model.getName(), model);
    }

    public List<UserCacheModel> getByNameStartingWith(String start) {
        String finalStart = start.toLowerCase();
        return userUuidLookup.values().stream().filter(m -> m.getName().startsWith(finalStart)).collect(Collectors.toList());
    }
}
