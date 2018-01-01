package me.illgilp.worldeditglobalizerbungee.config;

import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.yamlconfigurator.config.Config;
import me.illgilp.yamlconfigurator.config.ConfigManager;
import me.illgilp.yamlconfigurator.config.annotations.ConfigClass;
import me.illgilp.yamlconfigurator.config.annotations.ConfigEntry;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.chat.TextComponent;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

@ConfigClass(name = "MainConfig",file = "{DATAFOLDER}/config.yml",header = {
        "WorldEditGlobalizer configuration file"
})
public class MainConfig extends Config {
    @Override
    public void onFileCreation() {
        WorldEditGlobalizerBungee.getInstance().getLogger().info("config.yml not found -> create ...");
    }

    @Override
    public void onRegister() {
        try {
            maxClipboardBytes = Long.parseLong(maxBytes);
        }catch (NumberFormatException e){
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            try {
                maxClipboardBytes = Long.parseLong(""+engine.eval(maxBytes));
            } catch (ScriptException ex) {
                BungeeCord.getInstance().getConsole().sendMessage(new TextComponent(TextComponent.fromLegacyText("[WorldEditGlobalizer] §cWARNING! config.yml contains errors! Errors have been replaced with the default value!")));
                maxBytes = new MainConfig().maxBytes;
                maxClipboardBytes = 1024*1024*30;
                saveConfig();
            }
        }
    }

    @Override
    public void onUnregister() {

    }

    @Override
    public void onReload() {
        try {
            maxClipboardBytes = Long.parseLong(maxBytes);
        }catch (NumberFormatException e){
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            try {
                maxClipboardBytes = Long.parseLong(""+engine.eval(maxBytes));
            } catch (ScriptException ex) {
                BungeeCord.getInstance().getConsole().sendMessage(new TextComponent(TextComponent.fromLegacyText("[WorldEditGlobalizer] §cWARNING! config.yml contains errors! Errors have been replaced with the default value!")));
                maxBytes = new MainConfig().maxBytes;
                maxClipboardBytes = 1024*1024*30;
                saveConfig();
            }
        }
        WorldEditGlobalizerBungee.getInstance().getLogger().info("config.yml has been reloaded");
    }

    @ConfigEntry(path = "language",shouldDefault = true,comments = {
            "With this you can change the language the plugin should use.",
            "Default possibilities are 'en'(english) or 'de'(german).",
            "If you wanna add a own language you have to set this to your wish-language and the plugin automatically creates a message file for you!",
            "This message file is by default in english. You can change all values in it. The message files supports UTF-8."
    })
    private String lang = "en";

    @ConfigEntry(path = "maxClipboardSize",shouldDefault = true,comments = {
            "With this you can change the max amount of bytes a clipboard can have to upload it.",
            "You can use math operations like '1024*1024*30'."
    })
    private String maxBytes = "1024*1024*30";

    @ConfigEntry(path = "keepClipboard",shouldDefault = true,comments = {
            "With this you can choose if a clipboard of a player should be reset after logging out or after bungeecord restart"
    })
    private boolean keepClipboard = false;

    @ConfigEntry(path = "prefix",shouldDefault = true,comments = {
            "With this you can change the prefix for all messages that are in a message file"
    })
    private String prefix = "&3WEG &7&l>> &r";

    public String getLanguage() {
        return lang;
    }

    public void setLanguage(String lang) {
        this.lang = lang;
    }

    private long maxClipboardBytes = 0;


    public void setMaxClipboardBytes(String maxBytes) {
        this.maxBytes = maxBytes;
    }

    public long getMaxClipboardBytes() {
        return maxClipboardBytes;
    }

    public boolean isKeepClipboard() {
        return keepClipboard;
    }

    public void setKeepClipboard(boolean keepClipboard) {
        this.keepClipboard = keepClipboard;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
