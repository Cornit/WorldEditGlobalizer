package me.illgilp.yamlconfigurator.config;

import me.illgilp.yamlconfigurator.config.annotations.ConfigClass;
import me.illgilp.yamlconfigurator.config.annotations.ConfigEntry;
import me.illgilp.yamlconfigurator.config.utils.StringUtils;
import me.illgilp.yamlconfigurator.configuration.ConfigurationSection;
import me.illgilp.yamlconfigurator.configuration.InvalidConfigurationException;
import me.illgilp.yamlconfigurator.configuration.file.YamlConfiguration;
import me.illgilp.yamlconfigurator.configuration.serialization.YamlSerializer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public abstract class Config implements YamlSerializer {
    private File configFile;
    private YamlConfiguration yaml;
    private String configName;
    private Map loaded;
    private Object config;

    public Config() {

    }


    public String getConfigName() {
        return this.configName;
    }

    public File getConfigFile() {
        return this.configFile;
    }

    public abstract void onFileCreation();

    public abstract void onRegister();

    public abstract void onUnregister();

    public abstract void onReload();

    public void load() {
        try {
            this.yaml.load(this.configFile, configName);
            loadFields();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void saveConfig() {
        try {
            addFields();
            String header = "";
            int i = 0;
            for (String str : config.getClass().getAnnotation(ConfigClass.class).header()) {
                i++;
                if (config.getClass().getAnnotation(ConfigClass.class).header().length == i) {
                    header += StringUtils.replacePlaceholder(str, yaml.getConfigManager());
                } else {
                    header += StringUtils.replacePlaceholder(str, yaml.getConfigManager()) + "\n";
                }
            }
            yaml.options().header(header);
            yaml.save(configFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String[]> getComments() {
        Map<String, String[]> comments = new HashMap<>();
        try {
            Class cls = config.getClass();
            List<Field> fields = new ArrayList<>();
            for (Field f : cls.getDeclaredFields()) {
                f.setAccessible(true);
                if (f.isAnnotationPresent(ConfigEntry.class)) {

                    ConfigEntry entry = f.getAnnotation(ConfigEntry.class);
                    comments.put(entry.path(), StringUtils.replacePlaceholderInArray(entry.comments(), yaml.getConfigManager()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return comments;
    }

    private void loadFields() {
        boolean exists = false;
        try {
            Class cls = config.getClass();
            List<Field> fields = new ArrayList<>();
            for (Field f : cls.getDeclaredFields()) {
                f.setAccessible(true);
                if (f.isAnnotationPresent(ConfigEntry.class)) {
                    exists = true;
                    ConfigEntry entry = f.getAnnotation(ConfigEntry.class);
                    if (entry.finalValue() == false) {
                        if (entry.shouldDefault()) {
                            if (f.get(config) == null) {
                                throw new NullPointerException("default fields cannot be null.");
                            } else {
                                if (yaml.get(entry.path()) == null) {
                                    yaml.set(entry.path(), f.get(config));
                                } else {
                                    if (f.getType() == String.class) {
                                        f.set(config, yaml.get(entry.path()) + "");
                                    } else {
                                        f.set(config, yaml.get(entry.path()));
                                    }
                                }
                            }
                        } else {
                            if (yaml.get(entry.path()) != null) {
                                f.set(config, yaml.get(entry.path()));
                            }
                        }
                    }
                }
            }
            if (!exists) {
                throw new NullPointerException(ConfigEntry.class.getName() + " couldn't be found in " + config.getClass().getName());
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void addFields() {
        boolean exists = false;
        try {
            Class cls = config.getClass();
            List<Field> fields = new ArrayList<>();
            for (Field f : cls.getDeclaredFields()) {
                f.setAccessible(true);
                if (f.isAnnotationPresent(ConfigEntry.class)) {
                    exists = true;
                    ConfigEntry entry = f.getAnnotation(ConfigEntry.class);
                    if (entry.shouldDefault()) {
                        if (f.get(config) == null) {
                            throw new NullPointerException("default fields cannot be null.");
                        } else {
                            if (!yaml.get(entry.path()).equals(f.get(config))) {
                                yaml.set(entry.path(), f.get(config));
                            }

                        }
                    } else {
                        yaml.set(entry.path(), f.get(config));
                    }
                }
            }
            if (!exists) {
                throw new NullPointerException(ConfigEntry.class.getName() + " couldn't be found in " + config.getClass().getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void set(String key, Object value) {
        yaml.set(key, value);
    }

    public Object get(String key) {
        return yaml.get(key);
    }

    public ConfigurationSection getConfigurationSection(String path) {
        return yaml.getConfigurationSection(path);
    }

    public Set<String> getKeys(boolean deep) {
        return yaml.getKeys(deep);
    }

    @Override
    public StringBuilder onLineSerialize(String line, String key, StringBuilder writeLines, ConfigManager configManager, Config config, int offset) {
        if (config.getComments().containsKey(key)) {
            if (writeLines.length() > 1) {
                if (writeLines.charAt(writeLines.length() - 2) != '\n') {
                    writeLines.append("\n");
                }
            }
            for (String comment : config.getComments().get(key)) {
                writeLines.append(new String(new char[offset]).replace("\0", " "));
                writeLines.append("# ");
                writeLines.append(comment);
                writeLines.append("\n");
            }
        }

        writeLines.append(line);
        writeLines.append("\n");
        if (config.getComments().containsKey(key)) {
            writeLines.append("\n");
        }
        return writeLines;

    }
}

