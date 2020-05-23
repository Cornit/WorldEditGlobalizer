package me.illgilp.worldeditglobalizerbukkit.config;

import me.illgilp.worldeditglobalizerbukkit.WorldEditGlobalizerBukkit;
import me.illgilp.yamlconfigurator.config.Config;
import me.illgilp.yamlconfigurator.config.annotations.ConfigClass;
import me.illgilp.yamlconfigurator.config.annotations.ConfigEntry;

@ConfigClass(name = "MainConfig", file = "{DATAFOLDER}/config.yml", header = {
        "WorldEditGlobalizerBukkit configuration file"
})
public class MainConfig extends Config {

    @ConfigEntry(path = "secretKey", shouldDefault = true, comments = {
        "This key makes sure that the connection between BungeeCord and Bukkit is safe, so this key must be identical to the key in the config of the BungeeCord-Plugin."
    })
    private String secretKey = "PUT KEY IN HERE";

    @Override
    public void onFileCreation() {
        WorldEditGlobalizerBukkit.getInstance().getLogger().info("config.yml not found -> create ...");
    }

    @Override
    public void onRegister() {
    }

    @Override
    public void onUnregister() {

    }

    @Override
    public void onReload() {
        WorldEditGlobalizerBukkit.getInstance().getLogger().info("config.yml has been reloaded");
    }

    public String getSecretKey() {
        return secretKey;
    }

}
