package me.illgilp.worldeditglobalizer.common.util.yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.reader.UnicodeReader;

public class YamlConfiguration {

    private Map<String, Object> root = new LinkedHashMap<>();
    private final File file;
    private final Yaml yaml;

    public YamlConfiguration(File file) {
        this.file = file;
        yaml = new Yaml(new SafeConstructor());
    }

    public Map<String, Object> getMap() {
        return root;
    }

    public void load() throws IOException {
        try (InputStream stream = getInputStream()) {
            read(yaml.load(new UnicodeReader(stream)));
        }
    }

    @SuppressWarnings("unchecked")
    private void read(Object input) throws YamlConfigurationException {
        try {
            if (null == input) {
                root = new LinkedHashMap<>();
            } else {
                root = new LinkedHashMap<>((Map<String, Object>) input);
            }
        } catch (ClassCastException e) {
            throw new YamlConfigurationException("Root document must be an key-value structure");
        }
    }

    private InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    @SuppressWarnings("unchecked")
    public Object getProperty(String path) {
        if (!path.contains(".")) {
            Object val = root.get(path);
            if (val == null) {
                throw new YamlConfigurationException("missing property '" + path + "'");
            }
            return val;
        }
        String[] parts = path.split("\\.");
        Map<String, Object> node = root;
        for (int i = 0; i < parts.length; i++) {
            Object o = node.get(parts[i]);
            if (o == null) {
                throw new YamlConfigurationException("missing property '" + path + "'");
            }
            if (i == parts.length - 1) {
                return o;
            }
            try {
                node = (Map<String, Object>) o;
            } catch (ClassCastException e) {
                throw new YamlConfigurationException("missing property '" + path + "'");
            }
        }
        throw new YamlConfigurationException("missing property '" + path + "'");
    }

    public String getString(String path) {
        Object o = getProperty(path);
        return o.toString();
    }

    public Integer getInt(String path) {
        Object o = getProperty(path);
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        throw new YamlConfigurationException("expecting integer for property '" + path + "'");
    }

    public Long getLong(String path) {
        Object o = getProperty(path);
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        throw new YamlConfigurationException("expecting long for property '" + path + "'");
    }

    public Double getDouble(String path) {
        Object o = getProperty(path);
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        throw new YamlConfigurationException("expecting double for property '" + path + "'");
    }

    public Boolean getBoolean(String path) {
        Object o = getProperty(path);
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        throw new YamlConfigurationException("expecting boolean for property '" + path + "'");
    }
}
