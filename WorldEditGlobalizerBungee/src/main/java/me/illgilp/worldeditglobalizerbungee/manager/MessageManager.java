package me.illgilp.worldeditglobalizerbungee.manager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.chat.chatevent.ChatEvent;
import me.illgilp.worldeditglobalizerbungee.message.MessageFile;
import me.illgilp.worldeditglobalizerbungee.message.template.CustomMessageFile;
import me.illgilp.worldeditglobalizerbungee.message.template.GermanMessageFile;
import me.illgilp.worldeditglobalizerbungee.player.Player;
import me.illgilp.worldeditglobalizerbungee.util.ComponentUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;

public class MessageManager {

    private static MessageManager instance;

    private File messageFolder;

    private Map<String, MessageFile> messageFiles = new HashMap<>();
    private String language = "en";
    private String prefix = "§3WEG §7§l>> §r";


    public MessageManager(File messageFolder) {
        this.messageFolder = messageFolder;
        messageFolder.mkdirs();
        messageFiles.put("en", new CustomMessageFile("en", new File(messageFolder, "messages_en.yml")));
        messageFiles.put("de", new GermanMessageFile(new File(messageFolder, "messages_de.yml")));

        for (File file : messageFolder.listFiles()) {
            if (file.getName().endsWith(".yml") && file.getName().startsWith("messages_")) {
                String lang = file.getName().replace(".yml", "").replace("messages_", "");
                messageFiles.put(lang, new CustomMessageFile(lang, file));
            }
        }

    }

    public static MessageManager getInstance() {
        if (instance == null)
            instance = new MessageManager(new File(WorldEditGlobalizerBungee.getInstance().getDataFolder(), "lang"));
        return instance;
    }

    public static String toJson(TextComponent textComponent) {
        if (textComponent == null) return "none";
        return ComponentSerializer.toString(textComponent);
    }

    public static TextComponent sendMessage(CommandSender commandSender, String path, Object... placeholders) {
        String message = getInstance().getMessageWithReplacedPlaceholders(getInstance().getMessageFile(getInstance().language).getMessage(path), placeholders);
        TextComponent textComponent = getInstance().toTextComponent(getInstance().getPrefix() + message);
        if (!message.equalsIgnoreCase("none")) {
            commandSender.sendMessage(textComponent);
        }
        return textComponent;
    }

    public static TextComponent sendActionBar(ProxiedPlayer commandSender, String path, Object... placeholders) {
        String message = getInstance().getMessageWithReplacedPlaceholders(getInstance().getMessageFile(getInstance().language).getMessage(path), placeholders);
        TextComponent textComponent = getInstance().toTextComponent(message);
        if (!message.equalsIgnoreCase("none")) {
            commandSender.sendMessage(ChatMessageType.ACTION_BAR, textComponent);
        }
        return textComponent;
    }

    public static TextComponent sendMessage(Player commandSender, ChatMessageType chatMessageType, String path, Object... placeholders) {
        String message = getInstance().getMessageWithReplacedPlaceholders(getInstance().getMessageFile(getInstance().language).getMessage(path), placeholders);
        TextComponent textComponent = getInstance().toTextComponent(getInstance().getPrefix() + message);
        if (!message.equalsIgnoreCase("none")) {
            commandSender.getProxiedPlayer().sendMessage(chatMessageType, textComponent);
        }
        return textComponent;
    }

    public static TextComponent getMessage(String path, Object... placeholders) {
        String message = getInstance().getMessageWithReplacedPlaceholders(getInstance().getMessageFile(getInstance().getLanguage()).getMessage(path), placeholders);
        if (message.equalsIgnoreCase("none")) {
            return null;
        } else {
            TextComponent textComponent = getInstance().toTextComponent(getInstance().getPrefix() + message);
            return textComponent;
        }
    }

    public static TextComponent getMessageOrEmpty(String path, Object... placeholders) {
        String message = getInstance().getMessageWithReplacedPlaceholders(getInstance().getMessageFile(getInstance().getLanguage()).getMessage(path), placeholders);
        if (message.equalsIgnoreCase("none")) {
            return ComponentUtils.of("");
        } else {
            TextComponent textComponent = getInstance().toTextComponent(getInstance().getPrefix() + message);
            return textComponent;
        }
    }

    public static String getRawMessageOrEmpty(String path, Object... placeholders) {
        String message = getInstance().getMessageWithReplacedPlaceholders(getInstance().getMessageFile(getInstance().getLanguage()).getMessage(path), placeholders);
        if (message.equalsIgnoreCase("none")) {
            return "";
        } else {
            return message;
        }
    }

    public static TextComponent getMessage(String lang, String path, Object... placeholders) {
        String message = getInstance().getMessageWithReplacedPlaceholders(getInstance().getMessageFile(lang).getMessage(path), placeholders);
        if (message.equalsIgnoreCase("none")) {
            return null;
        } else {
            TextComponent textComponent = getInstance().toTextComponent(getInstance().getPrefix() + message);
            return textComponent;
        }
    }

    public String getMessageWithReplacedPlaceholders(String message, Object... placeholders) {
        for (int i = 0; i < placeholders.length; i++) {
            message = message.replace("{" + i + "}", placeholders[i].toString());
        }
        return message;
    }

    public TextComponent toTextComponent(String message) {
        Pattern chatEventPattern = Pattern.compile("\\{\"chatevent\":(\\{.*?})}");
        String[] spl = chatEventPattern.split(message);
        List<ChatEvent> chatEvents = new ArrayList<>();
        Matcher matcher = chatEventPattern.matcher(message);
        while (matcher.find()) {
            chatEvents.add(ChatEvent.parse(new Gson().fromJson(matcher.group(0), JsonObject.class)));
        }

        TextComponent rootComponent = new TextComponent(TextComponent.fromLegacyText(""));

        for (int i = 0; i < spl.length; i++) {
            ChatEvent chatEvent = chatEvents.size() > i ? chatEvents.get(i) : null;
            rootComponent.addExtra(new TextComponent(TextComponent.fromLegacyText(spl[i])));
            if (chatEvent != null) {
                rootComponent.addExtra(chatEvent.toComponent());
            }
        }

        return rootComponent;
    }

    public String getRawMessage(String path) {
        return getMessageFile(language).getRawMessage(path);
    }

    public boolean hasMessageFile(String language) {
        return messageFiles.containsKey(language);
    }

    public MessageFile getMessageFile(String language) {
        if (!messageFiles.containsKey(language)) return messageFiles.get(getLanguage());
        return messageFiles.get(language);
    }

    public void addMessageFile(MessageFile messageFile) {
        messageFiles.put(messageFile.getLanguage(), messageFile);
    }

    public String[] getLanguages() {
        return new ArrayList<String>(messageFiles.keySet()).toArray(new String[0]);
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void reload() {
        instance = null;
        instance = new MessageManager(new File(WorldEditGlobalizerBungee.getInstance().getDataFolder(), "lang"));

    }

    public File getMessageFolder() {
        return messageFolder;
    }
}
