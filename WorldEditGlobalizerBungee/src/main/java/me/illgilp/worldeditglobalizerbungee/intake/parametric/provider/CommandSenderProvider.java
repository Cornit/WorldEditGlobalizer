package me.illgilp.worldeditglobalizerbungee.intake.parametric.provider;

import com.sk89q.intake.argument.ArgumentException;
import com.sk89q.intake.argument.CommandArgs;
import com.sk89q.intake.parametric.Provider;
import com.sk89q.intake.parametric.ProvisionException;
import net.md_5.bungee.api.CommandSender;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.List;

public class CommandSenderProvider implements Provider<CommandSender> {


    @Override
    public boolean isProvided() {
        return true;
    }

    @Nullable
    @Override
    public CommandSender get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException {
        return arguments.getNamespace().containsKey(CommandSender.class) ? arguments.getNamespace().get(CommandSender.class) : null;
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return null;
    }
}
