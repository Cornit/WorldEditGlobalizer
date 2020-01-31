package me.illgilp.worldeditglobalizerbungee.intake.parametric.provider;

import java.lang.annotation.Annotation;
import java.util.List;
import me.illgilp.intake.argument.ArgumentException;
import me.illgilp.intake.argument.CommandArgs;
import me.illgilp.intake.parametric.Provider;
import me.illgilp.intake.parametric.ProvisionException;
import net.md_5.bungee.api.CommandSender;

public class CommandSenderProvider implements Provider<CommandSender> {


    @Override
    public boolean isProvided() {
        return true;
    }

    @Override
    public CommandSender get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException {
        return arguments.getNamespace().containsKey(CommandSender.class) ? arguments.getNamespace().get(CommandSender.class) : null;
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return null;
    }
}
