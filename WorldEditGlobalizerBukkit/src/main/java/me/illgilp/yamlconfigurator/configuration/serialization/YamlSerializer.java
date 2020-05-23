package me.illgilp.yamlconfigurator.configuration.serialization;

import me.illgilp.yamlconfigurator.config.Config;
import me.illgilp.yamlconfigurator.config.ConfigManager;

public interface YamlSerializer {

    StringBuilder onLineSerialize(String line, String key, StringBuilder writeLine, ConfigManager configManager, Config config, int offset);

}
