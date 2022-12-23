package me.illgilp.worldeditglobalizer.server.bukkit.api.worldedit.adapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.illgilp.worldeditglobalizer.server.bukkit.util.Version;
import me.illgilp.worldeditglobalizer.server.core.api.WegServer;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class WorldEditAdapterFilter implements Comparable<WorldEditAdapterFilter> {


    private static WorldEditAdapter worldEditAdapter;

    public static WorldEditAdapter getWorldEditAdapter() {
        if (worldEditAdapter != null) {
            return worldEditAdapter;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(WorldEditAdapterFilter.class.getResourceAsStream("adapters.txt"), StandardCharsets.UTF_8))) {
            List<WorldEditAdapterFilter> filters = reader.lines()
                .map(s -> {
                    final String[] parts = s.split(";");
                    if (parts.length == 4) {
                        return new WorldEditAdapterFilter(
                            parts[0],
                            Version.fromString(parts[1]),
                            WorldEditPluginType.valueOf(parts[2]),
                            parts[3]
                        );
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
            for (WorldEditAdapterFilter filter : filters) {
                if (filter.isUsable()) {
                    try {
                        Class<?> rawAdapterClass = Class.forName(filter.className);
                        Class<? extends WorldEditAdapter> adapterClass = rawAdapterClass.asSubclass(WorldEditAdapter.class);
                        Constructor<? extends WorldEditAdapter> constructor = adapterClass.getDeclaredConstructor(WorldEditAdapterFilter.class);
                        worldEditAdapter = constructor.newInstance(filter);
                        return worldEditAdapter;
                    } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException |
                             InvocationTargetException e) {
                        WegServer.getInstance().getLogger()
                            .log(Level.SEVERE, "Could not create WorldEditAdapter class '" + filter.getClassName() + "'", e);
                        return null;
                    }
                }
            }
        } catch (IOException e) {
            WegServer.getInstance().getLogger()
                .log(Level.SEVERE, "Could not load WorldEditAdapters", e);
            return null;
        }
        return null;
    }


    private final String className;
    private final Version minMinecraftVersion;
    private final WorldEditPluginType worldEditPluginType;
    private final String worldEditPluginVersion;

    public boolean isUsable() {
        if (Bukkit.getPluginManager().isPluginEnabled(this.worldEditPluginType.getPluginName())) {
            return getMinecraftVersion()
                .map(v -> v.compareTo(this.minMinecraftVersion) >= 0)
                .orElse(false);
        }
        return false;
    }

    public String getClassName() {
        return className;
    }

    public Version getMinMinecraftVersion() {
        return this.minMinecraftVersion;
    }

    public WorldEditPluginType getWorldEditPluginType() {
        return this.worldEditPluginType;
    }

    public String getWorldEditPluginVersion() {
        return this.worldEditPluginVersion;
    }

    private Optional<Version> getMinecraftVersion() {
        Matcher mcVersionMatcher = Pattern.compile("v(\\d+)_+(\\d+)_+R(\\d+)")
            .matcher(Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3]);
        if (mcVersionMatcher.find()) {
            return Optional.of(new Version(Integer.parseInt(mcVersionMatcher.group(1)), Integer.parseInt(mcVersionMatcher.group(2))));
        }
        return Optional.empty();
    }

    @Override
    public int compareTo(@NotNull WorldEditAdapterFilter o) {
        final int versionCompareResult = o.minMinecraftVersion.compareTo(this.minMinecraftVersion);
        if (versionCompareResult == 0) {
            return Integer.compare(this.worldEditPluginType.ordinal(), o.worldEditPluginType.ordinal());
        }
        return versionCompareResult;
    }

    @Override
    public String toString() {
        return "WorldEditAdapterFilter{" +
            "className='" + className + '\'' +
            ", minMinecraftVersion=" + minMinecraftVersion +
            ", worldEditPluginType=" + worldEditPluginType +
            ", worldEditPluginVersion='" + worldEditPluginVersion + '\'' +
            '}';
    }
}
