package me.illgilp.worldeditglobalizer.server.core.api.permission;

import java.util.Optional;
import me.illgilp.worldeditglobalizer.common.permission.Permission;
import net.kyori.adventure.util.TriState;

public interface PermissionSubject {

    default boolean hasPermission(Permission permission) {
        return Optional.ofNullable(getPermissionValue(permission)).map(t -> t.toBooleanOrElse(true)).orElse(true);
    }

    TriState getPermissionValue(Permission permission);

}
