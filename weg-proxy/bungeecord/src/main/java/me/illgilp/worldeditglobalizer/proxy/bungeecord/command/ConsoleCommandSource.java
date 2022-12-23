package me.illgilp.worldeditglobalizer.proxy.bungeecord.command;

import lombok.RequiredArgsConstructor;
import me.illgilp.worldeditglobalizer.common.permission.Permission;
import me.illgilp.worldeditglobalizer.proxy.core.api.command.CommandSource;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ConsoleCommandSource implements CommandSource {

    private final Audience audience;

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
        this.audience.sendMessage(source, message, type);
    }

    @Override
    public TriState getPermissionValue(Permission permission, boolean allowCached) {
        return TriState.TRUE;
    }

}
