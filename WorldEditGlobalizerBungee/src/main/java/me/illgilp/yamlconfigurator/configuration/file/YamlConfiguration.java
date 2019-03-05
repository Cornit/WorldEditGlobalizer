package me.illgilp.yamlconfigurator.configuration.file;

import me.illgilp.yamlconfigurator.config.Config;
import me.illgilp.yamlconfigurator.config.ConfigManager;
import me.illgilp.yamlconfigurator.configuration.Configuration;
import me.illgilp.yamlconfigurator.configuration.ConfigurationSection;
import me.illgilp.yamlconfigurator.configuration.InvalidConfigurationException;
import org.apache.commons.lang3.Validate;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link Configuration} which saves all files in Yaml.
 * Note that this implementation is not synchronized.
 */
public class YamlConfiguration extends FileConfiguration {
    protected static final String COMMENT_PREFIX = "# ";
    protected static final String BLANK_CONFIG = "{}\n";
    private final DumperOptions yamlOptions = new DumperOptions();
    private final Representer yamlRepresenter = new YamlRepresenter();
    private final Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);
    private String configName;
    private Map<String, Object> data = new HashMap<>();
    private ConfigManager configManager;


    public YamlConfiguration(ConfigManager configManager) {
        this.configManager = configManager;
    }

    private static String join(List<String> list, String conjunction) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : list) {
            if (first) {
                first = false;
            } else {
                sb.append(conjunction);
            }
            sb.append(item);
        }

        return sb.toString();
    }

    /**
     * Creates a new {@link YamlConfiguration}, loading from the given file.
     * <p>
     * Any errors loading the Configuration will be logged and then ignored.
     * If the specified input is not a valid config, a blank config will be
     * returned.
     * <p>
     * The encoding used may follow the system dependent default.
     *
     * @param file Input file
     * @return Resulting configuration
     * @throws IllegalArgumentException Thrown if file is null
     */
    public static YamlConfiguration loadConfiguration(File file, ConfigManager configManager) {
        Validate.notNull(file, "File cannot be null");

        YamlConfiguration config = new YamlConfiguration(configManager);

        try {
            config.load(file);
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InvalidConfigurationException ex) {
            ex.printStackTrace();
        }

        return config;
    }

    /**
     * Creates a new {@link YamlConfiguration}, loading from the given stream.
     * <p>
     * Any errors loading the Configuration will be logged and then ignored.
     * If the specified input is not a valid config, a blank config will be
     * returned.
     *
     * @param stream Input stream
     * @return Resulting configuration
     * @throws IllegalArgumentException Thrown if stream is null
     * @see #load(InputStream)
     * @see #loadConfiguration(Reader, ConfigManager)
     * @deprecated does not properly consider encoding
     */
    @Deprecated
    public static YamlConfiguration loadConfiguration(InputStream stream, ConfigManager configManager) {
        Validate.notNull(stream, "Stream cannot be null");

        YamlConfiguration config = new YamlConfiguration(configManager);

        try {
            config.load(stream);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InvalidConfigurationException ex) {
            ex.printStackTrace();
        }

        return config;
    }

    /**
     * Creates a new {@link YamlConfiguration}, loading from the given reader.
     * <p>
     * Any errors loading the Configuration will be logged and then ignored.
     * If the specified input is not a valid config, a blank config will be
     * returned.
     *
     * @param reader input
     * @return resulting configuration
     * @throws IllegalArgumentException Thrown if stream is null
     */

    public static YamlConfiguration loadConfiguration(Reader reader, ConfigManager configManager) {
        Validate.notNull(reader, "Stream cannot be null");

        YamlConfiguration config = new YamlConfiguration(configManager);

        try {
            config.load(reader);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InvalidConfigurationException ex) {
            ex.printStackTrace();
        }

        return config;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public String saveToString() {
        yamlOptions.setIndent(options().indent());
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlOptions.setAllowUnicode(SYSTEM_UTF);
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        if (configName == null) {
            try {
                throw new IllegalAccessException("config has been loaded wrongly!");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        String header = buildHeader();
        String dump;

        Integer depth = 0;

        ArrayList<String> keyChain = new ArrayList<>();
        String yamlString = yaml.dump(getValues(false));
        StringBuilder writeLines = new StringBuilder();
        for (String line : yamlString.split("\n")) {
            if (line.startsWith(new String(new char[depth]).replace("\0", " "))) {
                keyChain.add(line.split(":")[0].trim());
                depth = depth + 2;
            } else {
                if (line.startsWith(new String(new char[depth - 2]).replace("\0", " "))) {
                    keyChain.remove(keyChain.size() - 1);
                } else {
                    //Check how much spaces are infront of the line
                    int spaces = 0;
                    for (int i = 0; i < line.length(); i++) {
                        if (line.charAt(i) == ' ') {
                            spaces++;
                        } else {
                            break;
                        }
                    }

                    depth = spaces;

                    if (spaces == 0) {
                        keyChain = new ArrayList<>();
                        depth = 2;
                    } else {
                        ArrayList<String> temp = new ArrayList<>();
                        int index = 0;
                        for (int i = 0; i < spaces; i = i + 2, index++) {
                            temp.add(keyChain.get(index));
                        }

                        keyChain = temp;

                        depth = depth + 2;
                    }
                }

                keyChain.add(line.split(":")[0].trim());
            }

            String search;
            if (keyChain.size() > 0) {
                search = join(keyChain, ".");
            } else {
                search = "";
            }

            Config config = configManager.getConfig(configName);
            writeLines = config.onLineSerialize(line, search, writeLines, configManager, config, depth - 2);

        }

        dump = writeLines.toString();


        if (dump.equals(BLANK_CONFIG)) {
            dump = "";
        }

        return header + "\n" + dump;
    }

    @Override
    public void loadFromString(String contents) throws InvalidConfigurationException {
        Validate.notNull(contents, "Contents cannot be null");

        Map<?, ?> input;
        try {
            input = yaml.load(contents);
        } catch (YAMLException e) {
            throw new InvalidConfigurationException(e);
        } catch (ClassCastException e) {
            throw new InvalidConfigurationException("Top level is not a Map.");
        }

        String header = parseHeader(contents);
        if (header.length() > 0) {
            options().header(header);
        }

        if (input != null) {
            convertMapsToSections(input, this);
        }
    }

    protected void convertMapsToSections(Map<?, ?> input, ConfigurationSection section) {
        for (Map.Entry<?, ?> entry : input.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            if (value instanceof Map) {
                convertMapsToSections((Map<?, ?>) value, section.createSection(key));
            } else {
                section.set(key, value);
            }
        }
    }

    protected String parseHeader(String input) {
        String[] lines = input.split("\r?\n", -1);
        StringBuilder result = new StringBuilder();
        boolean readingHeader = true;
        boolean foundHeader = false;

        for (int i = 0; (i < lines.length) && (readingHeader); i++) {
            String line = lines[i];

            if (line.startsWith(COMMENT_PREFIX)) {
                if (i > 0) {
                    result.append("\n");
                }

                if (line.length() > COMMENT_PREFIX.length()) {
                    result.append(line.substring(COMMENT_PREFIX.length()));
                }

                foundHeader = true;
            } else if ((foundHeader) && (line.length() == 0)) {
                result.append("\n");
            } else if (foundHeader) {
                readingHeader = false;
            }
        }

        return result.toString();
    }

    @Override
    protected String buildHeader() {
        String header = options().header();

        if (options().copyHeader()) {
            Configuration def = getDefaults();

            if ((def != null) && (def instanceof FileConfiguration)) {
                FileConfiguration filedefaults = (FileConfiguration) def;
                String defaultsHeader = filedefaults.buildHeader();

                if ((defaultsHeader != null) && (defaultsHeader.length() > 0)) {
                    return defaultsHeader;
                }
            }
        }

        if (header == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        String[] lines = header.split("\r?\n", -1);
        boolean startedHeader = false;

        for (int i = lines.length - 1; i >= 0; i--) {
            builder.insert(0, "\n");

            if ((startedHeader) || (lines[i].length() != 0)) {
                builder.insert(0, lines[i]);
                builder.insert(0, COMMENT_PREFIX);
                startedHeader = true;
            }
        }

        return builder.toString();
    }

    @Override
    public YamlConfigurationOptions options() {
        if (options == null) {
            options = new YamlConfigurationOptions(this);
        }

        return (YamlConfigurationOptions) options;
    }

    public void load(File file, String configname) throws IOException, InvalidConfigurationException {
        this.configName = configname;
        super.load(file);
    }
}
