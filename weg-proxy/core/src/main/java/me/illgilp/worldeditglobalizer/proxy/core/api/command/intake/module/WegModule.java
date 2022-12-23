package me.illgilp.worldeditglobalizer.proxy.core.api.command.intake.module;

import me.illgilp.worldeditglobalizer.proxy.core.api.command.CommandSource;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.intake.annotation.SavedSchematicArg;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.intake.annotation.Source;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.intake.provider.CommandSourceProvider;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.intake.provider.SavedSchematicArgProvider;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.intake.provider.WegOfflinePlayerSourceProvider;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.intake.provider.WegPlayerSourceProvider;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegOfflinePlayer;
import me.illgilp.worldeditglobalizer.proxy.core.api.player.WegPlayer;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.AbstractModule;

public class WegModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(CommandSource.class).annotatedWith(Source.class).toProvider(new CommandSourceProvider());
        bind(WegPlayer.class).annotatedWith(Source.class).toProvider(new WegPlayerSourceProvider());
        bind(WegOfflinePlayer.class).toProvider(new WegOfflinePlayerSourceProvider());
        bind(String.class).annotatedWith(SavedSchematicArg.class).toProvider(new SavedSchematicArgProvider());
    }

}
