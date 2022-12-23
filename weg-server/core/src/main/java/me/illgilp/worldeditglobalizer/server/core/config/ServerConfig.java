package me.illgilp.worldeditglobalizer.server.core.config;

import java.io.IOException;
import me.illgilp.worldeditglobalizer.common.util.yaml.YamlConfiguration;

public class ServerConfig {

    private String signatureSecret;
    private boolean useFallbackMessageSending;

    private final YamlConfiguration config;

    public ServerConfig(YamlConfiguration config) {
        this.config = config;
    }

    public void load() throws IOException {
        this.config.load();
        this.signatureSecret = config.getString("signature.secret");
        this.useFallbackMessageSending = config.getBoolean("use-fallback-message-sending");
    }

    public String getSignatureSecret() {
        return signatureSecret;
    }

    public boolean isUseFallbackMessageSending() {
        return useFallbackMessageSending;
    }
}
