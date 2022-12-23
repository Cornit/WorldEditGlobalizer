package me.illgilp.worldeditglobalizer.proxy.core.api.command.intake.provider;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.CommandSource;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegPlayer;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.ArgumentException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.CommandArgs;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.MessageArgumentException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.Provider;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.ProvisionException;
import org.jetbrains.annotations.Nullable;

public class WegPlayerSourceProvider implements Provider<WegPlayer> {

    @Override
    public boolean isProvided() {
        return true;
    }

    @Nullable
    @Override
    public WegPlayer get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException {
        if (!arguments.getNamespace().containsKey(CommandSource.class)) {
            return null;
        }
        CommandSource commandSource = arguments.getNamespace().get(CommandSource.class);
        if (!(commandSource instanceof WegPlayer)) {
            throw new MessageArgumentException(
                MessageHelper.builder()
                    .translation(TranslationKey.COMMAND_ERROR_PLAYERS_ONLY)
            );
        }
        return (WegPlayer) commandSource;
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return Collections.emptyList();
    }

}
