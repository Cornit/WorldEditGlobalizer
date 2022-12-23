package me.illgilp.worldeditglobalizer.proxy.core.api.command.intake.provider;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;
import me.illgilp.worldeditglobalizer.proxy.core.api.WegProxy;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegOfflinePlayer;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.ArgumentException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.CommandArgs;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.MessageArgumentException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.Provider;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.ProvisionException;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.Nullable;

public class WegOfflinePlayerSourceProvider implements Provider<WegOfflinePlayer> {

    @Override
    public boolean isProvided() {
        return false;
    }

    @Nullable
    @Override
    public WegOfflinePlayer get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException {
        String name = arguments.next();
        Optional<WegOfflinePlayer> player = WegProxy.getInstance().getOfflinePlayer(name);
        if (!player.isPresent()) {
            throw new MessageArgumentException(MessageHelper.builder()
                .translation(TranslationKey.COMMAND_ERROR_ARGUMENT_PLAYER_NOT_FOUND)
                .tagResolver(Placeholder.unparsed("player_name", name)));
        }
        return player.orElse(null);
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return WegProxy.getInstance().getOfflinePlayersByNameStartingWith(prefix).stream()
            .map(WegOfflinePlayer::getName)
            .sorted(String::compareToIgnoreCase)
            .collect(Collectors.toList());
    }

    @Override
    public boolean needPermission(WegOfflinePlayer value, List<? extends Annotation> modifiers, boolean argumentProvided) {
        return Provider.super.needPermission(value, modifiers, argumentProvided);
    }
}
