package me.illgilp.worldeditglobalizer.preprocessor.adapters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import me.illgilp.worldeditglobalizer.server.bukkit.api.worldedit.adapter.WorldEditPluginType;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface AdapterFilter {

    int[] minMcVersion();

    WorldEditPluginType wePluginType();

    int[] wePluginVersion();

}
