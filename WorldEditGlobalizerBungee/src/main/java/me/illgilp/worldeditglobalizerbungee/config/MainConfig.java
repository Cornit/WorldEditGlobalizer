package me.illgilp.worldeditglobalizerbungee.config;

import java.util.UUID;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.util.StringMathParser;
import me.illgilp.yamlconfigurator.config.Config;
import me.illgilp.yamlconfigurator.config.annotations.ConfigClass;
import me.illgilp.yamlconfigurator.config.annotations.ConfigEntry;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.chat.TextComponent;

@ConfigClass(name = "MainConfig", file = "{DATAFOLDER}/config.yml", header = {
        "WorldEditGlobalizer configuration file"
})
public class MainConfig extends Config {

    @ConfigEntry(path = "language", shouldDefault = true, comments = {
            "With this you can change the language the plugin should use.",
            "Default possibilities are 'en'(english) or 'de'(german).",
            "If you wanna add a own language you have to set this to your wish-language and the plugin automatically creates a message file for you!",
            "This message file is by default in english. You can change all values in it. The message files supports UTF-8."
    })
    private String lang = "en";

    @ConfigEntry(path = "maxClipboardSize", shouldDefault = true, comments = {
            "With this you can change the max amount of bytes a clipboard can have to upload it.",
            "You can use math operations like '1024*1024*30'."
    })
    private String maxBytes = "1024*1024*30";

    @ConfigEntry(path = "keepClipboard", shouldDefault = true, comments = {
            "With this you can choose if a clipboard of a player should be reset after logging out or after bungeecord restart"
    })
    private boolean keepClipboard = false;

    @ConfigEntry(path = "prefix", shouldDefault = true, comments = {
        "With this you can change the prefix for all messages that are in a message file"
    })
    private String prefix = "&3WEG &7&l>> &r";

    private long maxClipboardBytes = 0;

    @ConfigEntry(path = "enableClipboardAutoDownload", shouldDefault = true, comments = {
        "With this you can enable/disable the automatic download of the clipboard."
    })
    private boolean enableClipboardAutoDownload = true;

    @ConfigEntry(path = "enableClipboardAutoUpload", shouldDefault = true, comments = {
        "With this you can enable/disable the automatic upload of the clipboard."
    })
    private boolean enableClipboardAutoUpload = true;

    @ConfigEntry(path = "secretKey", shouldDefault = true, comments = {
        "This key makes sure that the connection between BungeeCord and Bukkit is safe, so this key must be identical to the key in every config of all subservers that have WEG installed."
    })
    private String secretKey = UUID.randomUUID().toString().replace("-","") + UUID.randomUUID().toString().replace("-","");

    @Override
    public void onFileCreation() {
        WorldEditGlobalizerBungee.getInstance().getLogger().info("config.yml not found -> create ...");
    }

    @Override
    public void onRegister() {
        try {
            maxClipboardBytes = StringMathParser.parseString(maxBytes);
            if (maxClipboardBytes <= 0) {
                throw new NumberFormatException("");
            }
        } catch (NumberFormatException e) {
            BungeeCord.getInstance().getConsole().sendMessage(new TextComponent(TextComponent.fromLegacyText("[WorldEditGlobalizer] §cWARNING! config.yml contains errors! Errors have been replaced with the default value!")));
            maxBytes = new MainConfig().maxBytes;
            maxClipboardBytes = 1024 * 1024 * 30;
            saveConfig("maxClipboardSize");
        }
    }

    @Override
    public void onUnregister() {

    }

    @Override
    public void onReload() {
        try {
            maxClipboardBytes = StringMathParser.parseString(maxBytes);
            if (maxClipboardBytes <= 0) {
                throw new NumberFormatException("");
            }
        } catch (NumberFormatException e) {
            BungeeCord.getInstance().getConsole().sendMessage(new TextComponent(TextComponent.fromLegacyText("[WorldEditGlobalizer] §cWARNING! config.yml contains errors! Errors have been replaced with the default value!")));
            maxBytes = new MainConfig().maxBytes;
            maxClipboardBytes = 1024 * 1024 * 30;
            saveConfig("maxClipboardSize");
        }
        WorldEditGlobalizerBungee.getInstance().getLogger().info("config.yml has been reloaded");
    }

    public String getLanguage() {
        return lang;
    }

    public void setLanguage(String lang) {
        this.lang = lang;
    }

    public long getMaxClipboardBytes() {
        return maxClipboardBytes;
    }

    public void setMaxClipboardBytes(String maxBytes) {
        this.maxBytes = maxBytes;
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

    public String getSecretKey() {
        return secretKey;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isEnableClipboardAutoDownload() {
        return enableClipboardAutoDownload;
    }

    public boolean isEnableClipboardAutoUpload() {
        return enableClipboardAutoUpload;
    }
}
