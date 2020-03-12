package me.illgilp.bukkit.configuration.serialization;


public interface YamlSerializer {

    StringBuilder onLineSerialize(String line, String key, StringBuilder writeLine, int offset);

}
