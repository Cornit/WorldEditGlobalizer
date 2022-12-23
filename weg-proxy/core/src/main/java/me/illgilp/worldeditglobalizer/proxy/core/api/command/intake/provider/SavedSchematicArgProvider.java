package me.illgilp.worldeditglobalizer.proxy.core.api.command.intake.provider;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.CommandSource;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegOfflinePlayer;
import me.illgilp.worldeditglobalizer.proxy.core.api.schematic.WegSchematic;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.ArgumentException;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.CommandArgs;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.Namespace;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.Provider;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.ProvisionException;

public class SavedSchematicArgProvider implements Provider<String> {


    @Override
    public boolean isProvided() {
        return false;
    }

    @Override
    public String get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException {
        return arguments.next();
    }

    @Override
    public List<String> getSuggestions(Namespace namespace, String prefix) {
        return Stream.of(namespace.get(CommandSource.class))
            .filter(Objects::nonNull)
            .filter(commandSource -> commandSource instanceof WegOfflinePlayer)
            .map(commandSource -> (WegOfflinePlayer) commandSource)
            .map(WegOfflinePlayer::getSchematicContainer)
            .flatMap(wegSchematicContainer -> {
                try {
                    return wegSchematicContainer.getSchematics().stream();
                } catch (IOException e) {
                    return Stream.empty();
                }
            })
            .map(WegSchematic::getName)
            .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
            .sorted().collect(Collectors.toList());
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return Collections.emptyList();
    }
}
