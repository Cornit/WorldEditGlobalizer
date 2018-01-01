package me.illgilp.worldeditglobalizerbungee.manager;

import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import me.illgilp.worldeditglobalizerbungee.message.MessageFile;
import me.illgilp.worldeditglobalizerbungee.message.template.CustomMessageFile;
import me.illgilp.worldeditglobalizerbungee.message.template.GermanMessageFile;
import me.illgilp.worldeditglobalizerbungee.util.ComponentUtils;
import net.md_5.bungee.api.CommandSender;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private static MessageManager instance;

    private File messageFolder;

    private Map<String,MessageFile> messageFiles = new HashMap<>();
    private String language = "en";
    private String prefix = "§3WEG §7§l>> §r";



    public MessageManager(File messageFolder) {
        this.messageFolder = messageFolder;
        messageFolder.mkdirs();
        messageFiles.put("en", new CustomMessageFile("en",new File(messageFolder,"messages_en.yml")));
        messageFiles.put("de", new GermanMessageFile(new File(messageFolder,"messages_de.yml")));

        for(File file : messageFolder.listFiles()){
            if(file.getName().endsWith(".yml")&&file.getName().startsWith("messages_")){
                String lang = file.getName().replace(".yml","").replace("messages_","");
                messageFiles.put(lang,new CustomMessageFile(lang,file));
            }
        }

    }

    public String getMessageWithReplacedPlaceholders(String message, Object... placeholders){
        for(int i = 0; i<placeholders.length;i++){
            message = message.replace("{"+i+"}",placeholders[i].toString());
        }
        return message;
    }

    public String getRawMessage(String path){
        return getMessageFile(language).getRawMessage(path);
    }

    public boolean hasMessageFile(String language){
        return messageFiles.containsKey(language);
    }

    public MessageFile getMessageFile(String language){
        if(!messageFiles.containsKey(language))return messageFiles.get(getLanguage());
        return messageFiles.get(language);
    }

    public void addMessageFile(MessageFile messageFile){
        messageFiles.put(messageFile.getLanguage(),messageFile);
    }

    public String[] getLanguages(){
        return new ArrayList<String>(messageFiles.keySet()).toArray(new String[0]);
    }

    public static MessageManager getInstance() {
        if(instance == null) instance = new MessageManager(new File(WorldEditGlobalizerBungee.getInstance().getDataFolder(),"lang"));
        return instance;
    }

    public static String sendMessage(CommandSender commandSender, String path, Object... placeholders){
        String message = getInstance().getMessageWithReplacedPlaceholders(getInstance().getMessageFile(getInstance().language).getMessage(path),placeholders);
        if(!message.equalsIgnoreCase("none")) {
            commandSender.sendMessage(ComponentUtils.addText(null, getInstance().getPrefix() + message));
        }
        return message;
    }

    public static String getMessage(String lang, String path, Object... placeholders){
        String message = getInstance().getMessageWithReplacedPlaceholders(getInstance().getMessageFile(lang).getMessage(path),placeholders);
        if(message.equalsIgnoreCase("none")){
            return "none";
        }else {
            return getInstance().getPrefix()+message;
        }
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

    public void reload(){
        instance = null;
        instance = new MessageManager(new File(WorldEditGlobalizerBungee.getInstance().getDataFolder(),"lang"));

    }

    public File getMessageFolder() {
        return messageFolder;
    }
}
