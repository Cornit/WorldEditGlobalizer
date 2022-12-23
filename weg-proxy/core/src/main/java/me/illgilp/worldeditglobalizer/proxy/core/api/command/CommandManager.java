package me.illgilp.worldeditglobalizer.proxy.core.api.command;

import java.util.List;

public interface CommandManager {

    void handleCommandLine(CommandSource source, String commandLine);

    List<String> getSuggestions(CommandSource source, String commandLine);


}
