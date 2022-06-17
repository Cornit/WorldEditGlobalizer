package me.illgilp.worldeditglobalizerbungee.storage.cache;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Date;
import java.util.UUID;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.json.data.Data;
import me.illgilp.worldeditglobalizerbungee.json.data.DataLoader;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;

public class UserCacheModel {

    private UUID uuid;
    private String name;
    private String displayName;
    private Date lastLogin;
    private JsonObject data;

    public UserCacheModel() {
    }

    public UserCacheModel(UUID uuid, String name, String displayName) {
        this.uuid = uuid;
        this.name = name != null ? name.toLowerCase() : null;

        this.displayName = displayName;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name.toLowerCase();
    }

    public String getDisplayName() {
        return displayName;
    }

    public Date getLastLogin() {
        return lastLogin == null ? this.lastLogin = new Date() : lastLogin;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public void setName(String name) {
        this.name = name.toLowerCase();
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public JsonObject getData() {
        if (this.data == null) {
            this.data = new JsonObject();
        }
        return this.data;
    }

    public void addDataValue(String key, Data data) {
        JsonObject jsonObject = getData();
        jsonObject.add(key, data.write());
    }

    public void removeDataValue(String key) {
        JsonObject jsonObject = getData();
        jsonObject.remove(key);
    }

    public <T extends Data> T getDataValue(String key, Class<T> type) {
        JsonObject jsonObject = getData();
        if (!jsonObject.has(key)) {
            return null;
        }
        if (!jsonObject.get(key).isJsonObject()) {
            return null;
        }
        return DataLoader.loadData(jsonObject.getAsJsonObject(key), type);
    }

    public boolean read(PacketDataSerializer serializer) {
        this.uuid = serializer.readUUID();
        this.name = serializer.readString();
        this.displayName = serializer.readString();
        this.lastLogin = new Date(serializer.readVarLong());
        try {
            this.data = new Gson().fromJson(serializer.readString(), JsonObject.class);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void write(PacketDataSerializer serializer) {
        serializer.writeUUID(this.uuid);
        serializer.writeString(this.name);
        serializer.writeString(this.displayName);
        serializer.writeVarLong(this.lastLogin.getTime());
        serializer.writeString(new Gson().toJson(this.data == null ? this.data = new JsonObject() : this.data));
    }


    @Override
    public String toString() {
        return "UserCacheModel{" +
            "uuid=" + uuid +
            ", name='" + name + '\'' +
            ", displayName='" + displayName + '\'' +
            ", data='" + data + '\'' +
            '}';
    }

    public void save() {
        WorldEditGlobalizerBungee.getInstance().getUserCache().addToCache(this);
        WorldEditGlobalizerBungee.getInstance().getUserCache().save();
    }
}
