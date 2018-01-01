package org.bukkit.configuration.serialization;


public interface YamlSerializer {

    StringBuilder onLineSerialize(String line, String key, StringBuilder writeLine, int offset);

}
