package me.illgilp.worldeditglobalizerbungee.intake.parametric.module;

import me.illgilp.intake.parametric.AbstractModule;
import me.illgilp.worldeditglobalizerbungee.intake.parametric.annotation.SavedSchematicArg;
import me.illgilp.worldeditglobalizerbungee.intake.parametric.provider.CommandSenderProvider;
import me.illgilp.worldeditglobalizerbungee.intake.parametric.provider.OfflinePlayerProvider;
import me.illgilp.worldeditglobalizerbungee.intake.parametric.provider.PlayerProvider;
import me.illgilp.worldeditglobalizerbungee.intake.parametric.provider.SavedSchematicArgProvider;
import me.illgilp.worldeditglobalizerbungee.player.OfflinePlayer;
import me.illgilp.worldeditglobalizerbungee.player.Player;
import net.md_5.bungee.api.CommandSender;

public class WEGModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CommandSender.class).toProvider(new CommandSenderProvider());
        bind(Player.class).toProvider(new PlayerProvider());
        bind(String.class).annotatedWith(SavedSchematicArg.class).toProvider(new SavedSchematicArgProvider());
        bind(OfflinePlayer.class).toProvider(new OfflinePlayerProvider());
    }
}
