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

public class GermanMessageFile implements MessageFile {

    private File file;
    private YamlConfiguration configuration;
    private Map<String, Object> defaults = new HashMap<>();

    public GermanMessageFile(File file) {
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
        configuration.options().header("Alle Nachrichten können durch das Ersetzen der Nachricht mit 'none' deaktiviert werden.");
        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getLanguage() {
        return "de";
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
        defaults.put("timeFormat", "dd. MM. yyyy HH:mm:ss");

        defaults.put("clipboard.start.uploading", "&dDeine Zwischenablage wird hochgeladen...");
        defaults.put("clipboard.finish.uploading", "&dDeine Zwischenablage wurde hochgeladen! (Größe: {0})");

        defaults.put("clipboard.start.downloading", "&dDeine Zwischenablage wird auf den Server heruntergeladen, auf dem du dich gerade befindest...");
        defaults.put("clipboard.finish.downloading", "&dDeine Zwischenablage wurde heruntergeladen! (Größe: {0})");

        defaults.put("clipboard.error.downloading", "&cEs ist eine Fehler aufgetreten! Verwende '/weg download' um deine Zwischenablage herunterzuladen!");

        defaults.put("clipboard.clear", "&dDeine Zwischenablage ist nun leer!");

        defaults.put("clipboard.empty.own", "&cDeine Zwischenablage ist leer!");
        defaults.put("clipboard.empty.other", "&c{0}'s Zwischenablage ist leer!");

        defaults.put("clipboard.tooBig", "&cDeine Zwischenablage ist zu groß! (Max: {0} Zwischenablage: {1})");

        defaults.put("command.usage-message", "&cFalscher Syntax! Verwende: {0}");

        defaults.put("command.server.cannotUse.secretKeyNotSet", "&cDer Sicherheitsschlüssel in der Config von diesem Unterserver ist nicht gesetzt!");
        defaults.put("command.server.cannotUse.pluginNotInstalled", "&cDas Plugin ist nicht auf diesem Unterserver installiert!");
        defaults.put("command.server.cannotUse.incompatibleVersion", "&cDie Version von dem BungeeCord-Plugin(&6{0}&c) ist nicht kompatibel mit der Version dieses Unterservers: &8'{1}'&c(&6{2}&c)!\n" +
            "&cPlease use /weg syncVersions to sync the versions between BungeeCord and Bukkit/Spigot.");
        defaults.put("command.server.cannotUse.incorrectSecretKey", "&cDer Sicherheitsschlüssel in der Config von diesem Unterserver ist nicht korrekt!");

        defaults.put("command.start.syncversions", "&dDas Plugin wird auf den Unterserver heruntergeladen...");
        defaults.put("command.finish.syncversions", "&dDie Versionen wurden synchronisiert! Bitte starte den Unterserver neu, damit die Änderungen übernommen werden.");
        defaults.put("command.error.syncversions.tryAgain", "&cDas Plugin konnte nicht korrekt heruntergeladen werden! Versuche es erneut...");
        defaults.put("command.error.syncversions.failed", "&cDas Plugin konnte nicht korrekt heruntergeladen werden! Bitte versuche es später noch einmal!");

        defaults.put("command.permissionDenied", "&cDu hast keinen Zugriff auf diesen Befehl!");

        defaults.put("command.console", "&cNur Spieler können diesen Befehl ausführen!");

        defaults.put("timedOut", "&cDie aktuelle Aufgabe braucht länger als {0} Millisekunden und wird nun beendet!");

        defaults.put("command.start.reload", "&7Alle Configs und alle Nachrichten-Dateien werden neugeladen...");
        defaults.put("command.finish.reload", "&aAlle Configs und alle Nachrichten-Dateien wurden neugeladen!");

        defaults.put("command.playerNotFound", "&cDer Spieler '&6{0}&c' existiert nicht!");

        defaults.put("command.clear.success", "&aDie Zwischenablage von '&6{0}&c' wurde geleert!");

        defaults.put("command.info.format", "&3Info's über &6{0}&3:\n" +
                "&6&l>> &r&aName &7= &f{1}\n" +
                "&6&l>> &r&aUUID &7= &f{2}\n" +
                "&6&l>> &r&aUpload Datum &7= &f{3}\n" +
                "&6&l>> &r&aZwischenablagen Größe &7= &f{4}");

        defaults.put("command.stats.format", "&3Statistiken:\n" +
                "&6&l>> &r&aGespeicherte Zwischenablagen &7= &f{0}\n" +
                "&6&l>> &r&aTotaler Speicherverbrauch &7= &f{1}\n" +
                "&6&l>> &r&aDurchschnittlicher Speicherverbrauch &7= &f{2}");

        defaults.put("command.schematic.list", "&7Verfügbare Schematics [{0}/{1}]:\n&2{2}");
        defaults.put("command.schematic.save", "&aDeine Zwischenablage wurde in eine Schematic-Datei gespeichert!");
        defaults.put("command.schematic.delete.success", "&aDie Schematic-Datei wurde gelöscht!");
        defaults.put("command.schematic.delete.error", "&cDie Schematic-Datei konnte nicht gelöscht werden! Tipp: Starte den BungeeCord-Server neu und probiere es erneut!");
        defaults.put("command.schematic.delete", "&aDie Schematic-Datei wurde gelöscht!");
        defaults.put("command.schematic.load.notFound", "&cEine Schematic-Datei mit diesem Namen existiert nicht!");
        defaults.put("command.schematic.load.success", "&aDie Schematic-Datei wurde in deine Zwischenablage geladen! Wenn der Download fertig ist, kannst du die Schematic mit '//paste' einfügen!");

        defaults.put("invalid.number", "&cBitte gib eine gültige Nummer an!");

        defaults.put("incompatible.version", "&cDie Version von dem BungeeCord-Plugin(&6{0}&c) ist nicht kompatibel mit der Version des Unterservers: &8'{1}'&c(&6{2}&c)!");

        defaults.put("clipboard.unknownFormat", "&cDie heruntergeladene Zwischenablage/Schematic ist in einem falschen Format, leere diese um den Fehler zu beheben.");
        defaults.put("clipboard.tooNew", "&cDie heruntergeladene Zwischenablage/Schematic wurde in einer neueren Version von Minecraft erstellt und kann somit hier nicht verwendet werden!");

        defaults.put("update.notify", "&3Es gibt eine neue Version von diesem Plugin!\n" +
                "&6&l>> &r&aAktuelle Version &7= &f{0}\n" +
                "&6&l>> &r&aNeuste Version &7= &f{1}\n" +
                "&6&l>> &r&aUpdate Nachricht &7= &f{2}\n" +
                "&6&l>> &r&aDownload Link &7= &f{3}");

        defaults.put("command.help.discord", "&7Du brauchst Hilfe? Joine einfach unserem Discord Server. &7https://discord.gg/B8BEaNV");

        defaults.put("actionbar.progress.upload", "Lade hoch... |||||||||||||||||||||||||||||||||||||||||||||||||| {0}%");
        defaults.put("actionbar.progress.download", "Lade herunter... |||||||||||||||||||||||||||||||||||||||||||||||||| {0}%");
        defaults.put("actionbar.progress.setClipboard", "&3Setze Zwischenablage...");

        defaults.put("chat.function.notAvailable", "&cDiese Funktion ist nicht mehr verfügbar!");
        defaults.put("chat.function.error", "&cEin Fehler ist während der Ausführung dieser Funktion aufgetreten!");


        defaults.put("chat.box.page.number", "&3Seite &6{0} &3von &6{1}");
        defaults.put("chat.box.page.next.tooltip", "Nächste Seite");
        defaults.put("chat.box.page.previous.tooltip", "Vorherige Seite");


        defaults.put("chat.box.schematic.list.title", "&7Verfügbare Schematics");

        defaults.put("chat.box.schematic.list.button.load.title", "Laden");
        defaults.put("chat.box.schematic.list.button.load.tooltip", "Klicke um die Schematic '{0}' zu laden");

        defaults.put("chat.box.schematic.list.button.delete.title", "Löschen");
        defaults.put("chat.box.schematic.list.button.delete.tooltip", "Klicke um die Schematic '{0}' zu löschen");

        defaults.put("chat.box.schematic.delete.title", "Wirklich?");
        defaults.put("chat.box.schematic.delete.message", "Möchtest du wirklich die Schematic '{0}' löschen?");
        defaults.put("chat.box.schematic.delete.button.confirm.title", "Bestätigen");
        defaults.put("chat.box.schematic.delete.button.confirm.tooltip", "Klicke um zu bestätigen");

    }
}
