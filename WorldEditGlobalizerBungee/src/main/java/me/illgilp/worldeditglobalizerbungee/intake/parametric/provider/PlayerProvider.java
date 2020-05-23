package me.illgilp.worldeditglobalizerbungee.intake.parametric.provider;

import java.lang.annotation.Annotation;
import java.util.List;
import me.illgilp.intake.argument.ArgumentException;
import me.illgilp.intake.argument.CommandArgs;
import me.illgilp.intake.argument.CommandCancelException;
import me.illgilp.intake.parametric.Provider;
import me.illgilp.intake.parametric.ProvisionException;
import me.illgilp.worldeditglobalizerbungee.manager.MessageManager;
import me.illgilp.worldeditglobalizerbungee.player.Player;
import net.md_5.bungee.api.CommandSender;

public class PlayerProvider implements Provider<Player> {


    @Override
    public boolean isProvided() {
        return true;
    }

    @Override
    public Player get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException, CommandCancelException {
        if (!arguments.getNamespace().containsKey(CommandSender.class)) {
            return null;
        }

        CommandSender commandSender = arguments.getNamespace().get(CommandSender.class);
        if (!(commandSender instanceof Player)) {
            MessageManager.sendMessage(commandSender, "command.console");
            throw CommandCancelException.INSTANCE;
        }
        return (Player) commandSender;
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return null;
    }
}
