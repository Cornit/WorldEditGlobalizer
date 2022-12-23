package me.illgilp.worldeditglobalizer.server.core.api.player;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import me.illgilp.worldeditglobalizer.common.config.CommonProxyConfig;
import me.illgilp.worldeditglobalizer.server.core.api.clipboard.WegClipboard;
import me.illgilp.worldeditglobalizer.server.core.api.permission.PermissionSubject;
import me.illgilp.worldeditglobalizer.server.core.server.WegServerConnection;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.text.Component;

public interface WegPlayer extends Audience, Identified, PermissionSubject {

    UUID getUniqueId();

    String getName();

    Component getDisplayName();

    Locale getLocale();

    WegServerConnection getConnection();

    Optional<WegClipboard> getClipboard();

    boolean setClipboard(byte[] data, int hashCode);

    boolean uploadClipboard();

    CommonProxyConfig getProxyConfig();

    boolean isAutoUploadReady();

}
