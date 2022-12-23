package me.illgilp.worldeditglobalizer.proxy.core.api.command;

import me.illgilp.worldeditglobalizer.proxy.core.api.permission.PermissionSubject;
import net.kyori.adventure.audience.Audience;

public interface CommandSource extends Audience, PermissionSubject {
}
