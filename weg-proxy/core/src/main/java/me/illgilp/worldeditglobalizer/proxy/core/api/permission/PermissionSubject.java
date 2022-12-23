package me.illgilp.worldeditglobalizer.proxy.core.api.permission;

import java.util.Optional;
import me.illgilp.worldeditglobalizer.common.permission.Permission;
import net.kyori.adventure.util.TriState;

public interface PermissionSubject {

    default boolean hasPermission(Permission permission, boolean allowCached) {
        return Optional.ofNullable(getPermissionValue(permission, allowCached))
            .map(t -> t.toBooleanOrElse(false))
            .orElse(false);
    }

    default boolean hasPermission(Permission permission) {
        return hasPermission(permission, false);
    }

    TriState getPermissionValue(Permission permission, boolean allowCached);

}
