package me.illgilp.worldeditglobalizerbungee.storage.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.json.data.Data;
import me.illgilp.worldeditglobalizerbungee.json.data.DataLoader;
import me.illgilp.worldeditglobalizerbungee.storage.table.UserCacheTable;
import org.apache.commons.codec.digest.DigestUtils;

@DatabaseTable(tableName = "usercache")
public class UserCacheModel {

    @DatabaseField(columnName = "uuid", id=true)
    private UUID uuid;

    @DatabaseField(columnName = "name")
    private String name;

    @DatabaseField(columnName = "displayName")
    private String displayName;

    @DatabaseField(columnName = "last_update")
    private Date lastUpdate;

    @DatabaseField(columnName = "data", dataType = DataType.LONG_STRING)
    private String data;

    @DatabaseField(columnName = "hash")
    private String hash;

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

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public String getHash() {
        return hash;
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

    public JsonObject getData() {
        if (this.data == null) this.data = "{}";
        return new Gson().fromJson(this.data, JsonObject.class);
    }

    public void addDataValue(String key, Data data) {
        if (this.data == null) this.data = "{}";
        JsonObject jsonObject = new Gson().fromJson(this.data, JsonObject.class);
        jsonObject.add(key, data.write());
        this.data = new Gson().toJson(jsonObject);
    }

    public void removeDataValue(String key) {
        if (this.data == null) this.data = "{}";
        JsonObject jsonObject = new Gson().fromJson(this.data, JsonObject.class);
        jsonObject.remove(key);
        this.data = new Gson().toJson(jsonObject);
    }

    public <T extends Data> T getDataValue(String key, Class<T> type) {
        if (this.data == null) this.data = "{}";
        JsonObject jsonObject = new Gson().fromJson(this.data, JsonObject.class);
        if (!jsonObject.has(key)) {
            return null;
        }

        if (!jsonObject.get(key).isJsonObject()) {
            return null;
        }
        return DataLoader.loadData(jsonObject.getAsJsonObject(key), type);
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

    private String createHash(String str) {

        return DigestUtils.shaHex(str.getBytes(StandardCharsets.UTF_8));
    }

    public void save() {
        String newHash = createHash(this.toString());
        if (!newHash.equals(this.hash)) {
            this.hash = newHash;
            this.lastUpdate = new Date();
            WorldEditGlobalizerBungee.getInstance().getDatabase().getTable(UserCacheTable.class).createOrUpdate(this);
        }
    }
}


