package me.illgilp.worldeditglobalizerbungee.message.template;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import me.illgilp.bukkit.configuration.InvalidConfigurationException;
import me.illgilp.bukkit.configuration.file.YamlConfiguration;
import me.illgilp.worldeditglobalizerbungee.message.MessageFile;
import net.md_5.bungee.api.ChatColor;

public class CustomMessageFile implements MessageFile {

    private File file;
    private YamlConfiguration configuration;
    private Map<String, Object> defaults = new HashMap<>();
    private String language;

    public CustomMessageFile(String language, File file) {
        this.language = language;
        this.file = file;
        if (!file.exists()) {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        configuration = new YamlConfiguration();
        try {
            configuration.load(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        addDefaults();
        configuration.addDefaults(defaults);
        configuration.options().copyDefaults(true);
        configuration.options().header("All messages can be disabled by typing 'none' as message.");
        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getLanguage() {
        return language;
    }

    @Override
    public String getDefaultMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', getDefaultString(path) + "");
    }

    @Override
    public String getRawDefaultMessage(String path) {
        return getDefaultString(path) + "";
    }

    @Override
    public Set<String> getKeySet() {
        return defaults.keySet();
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', getString(path));
    }

    @Override
    public String getRawMessage(String path) {
        return getString(path);
    }

    private String getString(String path) {
        return ((configuration.getString(path) + "").equalsIgnoreCase("null") ? "none" : configuration.getString(path));
    }

    private String getDefaultString(String path) {
        return ((defaults.get(path) + "").equalsIgnoreCase("null") ? "none" : defaults.get(path) + "");
    }

    private void addDefaults() {
        defaults.put("timeFormat", "MM. dd. yyyy hh:mm:ss a");

        defaults.put("clipboard.start.uploading", "&dYour clipboard will be uploaded to the network...");
        defaults.put("clipboard.finish.uploading", "&dYour clipboard has been uploaded to the network! (Size: {0})");

        defaults.put("clipboard.start.downloading", "&dYour clipboard will be downloaded to the server you are currently on!");
        defaults.put("clipboard.finish.downloading", "&dYour clipboard has been downloaded! (Size: {0})");

        defaults.put("clipboard.error.downloading", "&cAn error occurred! Use '/weg download' to download your clipboard!");

        defaults.put("clipboard.clear", "&dYour clipboard is now empty!");

        defaults.put("clipboard.empty.own", "&cYour clipboard is empty!");
        defaults.put("clipboard.empty.other", "&c{0}'s clipboard is empty!");

        defaults.put("clipboard.tooBig", "&cYour clipboard is too big to upload! (max: {0} clipboard: {1})");

        defaults.put("command.usage-message", "&cWrong syntax! Try: {0}");

        defaults.put("command.server.cannotUse.secretKeyNotSet", "&cThe secret key in the config of the bukkit plugin has not been set!");
        defaults.put("command.server.cannotUse.pluginNotInstalled", "&cThe plugin is not installed on this subserver.");
        defaults.put("command.server.cannotUse.incompatibleVersion", "&cIncompatible versions between BungeeCord-Plugin(&6{0}&c) and this subserver: &8'{1}'&c(&6{2}&c)!");
        defaults.put("command.server.cannotUse.incorrectSecretKey", "&cThe secret key set in the config of this subserver is not correct.");
        //defaults.put("command.cannotUse", "&cYou cannot use this command on this server!");

        defaults.put("command.permissionDenied", "&cYou are not permitted to use this command!");

        defaults.put("command.console", "&cOnly players can use this command!");

        defaults.put("timedOut", "&cThe current operation takes longer than {0} milliseconds and will be cancelled!");

        defaults.put("command.start.reload", "&7All configs and all message files will be reloaded...");
        defaults.put("command.finish.reload", "&aAll configs and all message files has been reloaded!");

        defaults.put("command.playerNotFound", "&cThe player '&6{0}&c' doesn't exists!");

        defaults.put("command.clear.success", "&aThe clipboard of '&6{0}&a' has been cleared!");

        defaults.put("command.info.format", "&3Info about &6{0}&3:\n" +
                "&6&l>> &r&aName &7= &f{1}\n" +
                "&6&l>> &r&aUUID &7= &f{2}\n" +
                "&6&l>> &r&aUpload date &7= &f{3}\n" +
                "&6&l>> &r&aClipboard size &7= &f{4}");

        defaults.put("command.stats.format", "&3Stats:\n" +
                "&6&l>> &r&aStored clipboards &7= &f{0}\n" +
                "&6&l>> &r&aTotal space usage &7= &f{1}\n" +
                "&6&l>> &r&aAverage space usage &7= &f{2}");

        defaults.put("command.schematic.list", "&7Available schematics [{0}/{1}]:\n&2{2}");
        defaults.put("command.schematic.save", "&aYour clipboard has been saved as schematic!");
        defaults.put("command.schematic.delete.success", "&aThe schematic file has been deleted!");
        defaults.put("command.schematic.delete.error", "&cThe schematic file cannot be deleted! Tip: Restart BungeeCord and try again!");
        defaults.put("command.schematic.load.notFound", "&cA schematic file with this name doesn't exists!");
        defaults.put("command.schematic.load.success", "&aThe schematic file has been loaded to your clipboard! After the download has finished you can paste it with '//paste'!");

        defaults.put("command.start.syncversions", "&dDownloading plugin to subserver...");
        defaults.put("command.finish.syncversions", "&dVersions have been synced! Please restart the subserver to apply the changes.");
        defaults.put("command.error.syncversions.tryAgain", "&cPlugin could not be downloaded correctly! Trying again...");
        defaults.put("command.error.syncversions.failed", "&cPlugin could not be downloaded correctly! Please try again later!");

        defaults.put("invalid.number", "&cPlease enter a valid number!");

        defaults.put("incompatible.version", "&cIncompatible versions between BungeeCord-Plugin(&6{0}&c) and subserver: &8'{1}'&c(&6{2}&c)!");

        defaults.put("clipboard.unknownFormat", "&cThe downloaded clipboard/schematic is in a wrong format, please clear it to fix the error.");
        defaults.put("clipboard.tooNew", "&cThe downloaded clipboard/schematic was made in a newer version of minecraft and cannot be used here.");

        defaults.put("update.notify", "&3There is a new Version for this Plugin!\n" +
                "&6&l>> &r&aCurrent &7= &f{0}\n" +
                "&6&l>> &r&aLatest &7= &f{1}\n" +
                "&6&l>> &r&aUpdate Message &7= &f{2}\n" +
                "&6&l>> &r&aDownload Link &7= &f{3}");

        defaults.put("command.help.discord", "&7You need help? Simply join our Discord Server. &7https://discord.gg/B8BEaNV");

        defaults.put("actionbar.progress.upload", "Uploading... |||||||||||||||||||||||||||||||||||||||||||||||||| {0}%");
        defaults.put("actionbar.progress.download", "Downloading... |||||||||||||||||||||||||||||||||||||||||||||||||| {0}%");
        defaults.put("actionbar.progress.setClipboard", "&3Setting clipboard...");

        defaults.put("chat.function.notAvailable", "&cThis function is not available anymore!");
        defaults.put("chat.function.error", "&cAn internal error occurred while attempting to perform this function!");


        defaults.put("chat.box.page.number", "&3Page &6{0} &3of &6{1}");
        defaults.put("chat.box.page.next.tooltip", "Next page");
        defaults.put("chat.box.page.previous.tooltip", "Previous page");

        
        defaults.put("chat.box.schematic.list.title", "&7Available schematics");
        
        defaults.put("chat.box.schematic.list.button.load.title", "Load");
        defaults.put("chat.box.schematic.list.button.load.tooltip", "Click to load schematic '{0}'");
        
        defaults.put("chat.box.schematic.list.button.delete.title", "Delete");
        defaults.put("chat.box.schematic.list.button.delete.tooltip", "Click to delete schematic '{0}'");

        defaults.put("chat.box.schematic.delete.title", "Really?");
        defaults.put("chat.box.schematic.delete.message", "Do you really want to delete the schematic '{0}'?");
        defaults.put("chat.box.schematic.delete.button.confirm.title", "Confirm");
        defaults.put("chat.box.schematic.delete.button.confirm.tooltip", "Click to confirm");

    }
}
