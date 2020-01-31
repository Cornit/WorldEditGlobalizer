package me.illgilp.worldeditglobalizerbungee.intake.parametric.module;

import me.illgilp.intake.parametric.AbstractModule;
import me.illgilp.worldeditglobalizerbungee.intake.parametric.provider.CommandSenderProvider;
import net.md_5.bungee.api.CommandSender;

public class WEGModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CommandSender.class).toProvider(new CommandSenderProvider());
    }
}
