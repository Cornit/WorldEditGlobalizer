package me.illgilp.worldeditglobalizer.server.bukkit.api.worldedit.adapter;

public enum WorldEditPluginType {

    FAST_ASYNC_WORLD_EDIT("FastAsyncWorldEdit"),
    WORLD_EDIT("WorldEdit"),


    ;

    private final String pluginName;

    WorldEditPluginType(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginName() {
        return pluginName;
    }
}
