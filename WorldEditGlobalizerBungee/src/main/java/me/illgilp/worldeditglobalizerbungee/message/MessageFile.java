package me.illgilp.worldeditglobalizerbungee.message;

import java.io.File;
import java.util.Locale;
import java.util.Set;

public interface MessageFile {

    String getLanguage();

    String getDefaultMessage(String path);

    String getRawDefaultMessage(String path);

    Set<String> getKeySet();

    File getFile();

    String getMessage(String path);

    String getRawMessage(String path);

}
