package me.illgilp.worldeditglobalizer.proxy.core.api.command.intake.provider;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.CommandSource;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.ArgumentException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.CommandArgs;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.Provider;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.ProvisionException;
import org.jetbrains.annotations.Nullable;

public class CommandSourceProvider implements Provider<CommandSource> {

    @Override
    public boolean isProvided() {
        return true;
    }

    @Nullable
    @Override
    public CommandSource get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException {
        return arguments.getNamespace().get(CommandSource.class);
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return Collections.emptyList();
    }

}
