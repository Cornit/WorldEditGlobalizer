package me.illgilp.yamlconfigurator.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.illgilp.yamlconfigurator.config.annotations.ConfigClass;
import me.illgilp.yamlconfigurator.config.utils.StringUtils;
import me.illgilp.yamlconfigurator.configuration.file.YamlConfiguration;

public class ConfigManager {
    private List<Config> registeredConfigs = new ArrayList();

    private Map<String, Object> placeholders = new HashMap<>();


    public ConfigManager() {
    }

    public void addPlaceholder(String placeholder, Object value) {
        placeholders.put(placeholder, value);
    }

    public Object getPlaceholder(String placeholder) {
        return placeholders.get(placeholder);
    }

    public Map<String, Object> getPlaceholders() {
        return placeholders;
    }

    public void registerConfig(Config config) {
        try {
            if (config.getClass().isAnnotationPresent(ConfigClass.class)) {
                Class cls = config.getClass().getSuperclass();
                Field conf = cls.getDeclaredField("config");
                conf.setAccessible(true);
                conf.set(config, config);

                Field yamlF = cls.getDeclaredField("yaml");
                yamlF.setAccessible(true);
                yamlF.set(config, new YamlConfiguration(this));

                Field confFile = cls.getDeclaredField("configFile");
                confFile.setAccessible(true);
                confFile.set(config, new File(StringUtils.replacePlaceholder(config.getClass().getAnnotation(ConfigClass.class).file(), this)));

                Field configName = cls.getDeclaredField("configName");
                configName.setAccessible(true);
                configName.set(config, StringUtils.replacePlaceholder(config.getClass().getAnnotation(ConfigClass.class).name(), this));
                this.registeredConfigs.add(config);
                if (!config.getConfigFile().exists()) {
                    try {
                        if (config.getConfigFile().getParentFile() != null)
                            config.getConfigFile().getParentFile().mkdirs();
                        config.getConfigFile().createNewFile();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    config.load();
                    config.saveConfig();
                    config.onFileCreation();
                    config.onRegister();
                } else {
                    config.load();
                    config.onRegister();
                }
            } else {
                throw new IllegalArgumentException(config.getClass().getName() + " doesn't contains ConfigClass annotation!");
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public void unregisterConfig(Config config) {
        if (this.registeredConfigs.contains(config)) {
            this.registeredConfigs.remove(config);
            config.onUnregister();
        }
    }

    public void unregisterConfig(String configName) {
        for (Config config : new ArrayList<>(this.registeredConfigs)) {
            if (config.getConfigName().equals(configName)) {
                this.registeredConfigs.remove(config);
                config.onUnregister();
            }
        }
    }

    public void reload(Config config) {
        if (isConfigRegistered(config)) {
            if (!config.getConfigFile().exists()) {
                try {
                    if (config.getConfigFile().getParentFile() != null) config.getConfigFile().getParentFile().mkdirs();
                    config.getConfigFile().createNewFile();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                config.saveConfig();
                config.onFileCreation();
            } else {
                config.load();
            }
            config.onReload();
        }
    }

    public void reload(String configName) {
        if (isConfigRegistered(configName)) {
            for (Config config : this.registeredConfigs) {
                if (config.getConfigName().equals(configName)) {
                    reload(config);
                    break;
                }
            }
        }
    }

    public void reloadAllConfigs() {
        for (Config config : this.registeredConfigs) {
            reload(config);
        }
    }

    public boolean isConfigRegistered(Config config) {
        return this.registeredConfigs.contains(config);
    }

    public boolean isConfigRegistered(String configName) {
        for (Config config : this.registeredConfigs) {
            if (config.getConfigName().equals(configName)) {
                return true;
            }
        }
        return false;
    }

    public Config getConfig(String configName) {
        if (isConfigRegistered(configName)) {
            for (Config config : this.registeredConfigs) {
                if (config.getConfigName().equals(configName)) {
                    return config;
                }
            }
        }
        return null;
    }
}
