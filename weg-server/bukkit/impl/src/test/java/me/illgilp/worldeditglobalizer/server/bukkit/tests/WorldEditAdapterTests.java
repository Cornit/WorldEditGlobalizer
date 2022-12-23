package me.illgilp.worldeditglobalizer.server.bukkit.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import me.illgilp.worldeditglobalizer.server.bukkit.api.worldedit.adapter.WorldEditAdapterFilter;
import me.illgilp.worldeditglobalizer.server.bukkit.api.worldedit.adapter.WorldEditPluginType;
import me.illgilp.worldeditglobalizer.server.bukkit.util.Version;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WorldEditAdapterTests {


    @Test
    public void test_WorldEditAdapterFilter_compare() {
        List<WorldEditAdapterFilter> filters = new ArrayList<>();
        for (int i = 19; i >= 8; i--) {
            for (WorldEditPluginType value : WorldEditPluginType.values()) {
                filters.add(createFilter(new Version(1, i), value));
            }
        }
        List<WorldEditAdapterFilter> sorted = filters.stream().sorted().collect(Collectors.toList());
        Assertions.assertEquals(filters, sorted);
    }

    private WorldEditAdapterFilter createFilter(Version version, WorldEditPluginType worldEditPluginType) {
        return new WorldEditAdapterFilter("", version, worldEditPluginType, "");
    }

}
