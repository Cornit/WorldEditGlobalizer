package me.illgilp.worldeditglobalizerbungee.intake.parametric.provider;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;
import me.illgilp.intake.argument.ArgumentException;
import me.illgilp.intake.argument.CommandArgs;
import me.illgilp.intake.parametric.Provider;
import me.illgilp.worldeditglobalizerbungee.manager.PlayerManager;
import me.illgilp.worldeditglobalizerbungee.player.OfflinePlayer;

public class OfflinePlayerProvider implements Provider<OfflinePlayer> {


    @Override
    public boolean isProvided() {
        return true;
    }

    @Override
    public OfflinePlayer get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException {
        String name = arguments.next();
        return PlayerManager.getInstance().getOfflinePlayer(name);
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return PlayerManager.getInstance().getOfflinePlayersNames(prefix).stream().sorted(String::compareToIgnoreCase).collect(Collectors.toList());
    }
}
