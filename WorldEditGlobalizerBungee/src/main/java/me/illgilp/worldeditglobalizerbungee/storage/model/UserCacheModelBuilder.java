package me.illgilp.worldeditglobalizerbungee.storage.model;

import java.util.UUID;

public class UserCacheModelBuilder {
    private UUID uuid;
    private String name;
    private String displayName;

    public UserCacheModelBuilder setUUID(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public UserCacheModelBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public UserCacheModelBuilder setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public UserCacheModel createUserCacheModel() {
        return new UserCacheModel(uuid, name, displayName);
    }
}