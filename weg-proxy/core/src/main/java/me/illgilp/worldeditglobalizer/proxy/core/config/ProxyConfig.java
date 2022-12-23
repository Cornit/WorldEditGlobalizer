package me.illgilp.worldeditglobalizer.proxy.core.config;

import java.io.IOException;
import me.illgilp.worldeditglobalizer.common.config.CommonProxyConfig;
import me.illgilp.worldeditglobalizer.common.util.yaml.YamlConfiguration;

public class ProxyConfig extends CommonProxyConfig {

    private String signatureSecret;
    private long maxClipboardSize;
    private boolean enableClipboardAutoUpload;
    private boolean enableClipboardAutoDownload;
    private boolean enableKeepClipboard;
    private boolean enableClipboardAutoCleanup;

    private final YamlConfiguration config;

    public ProxyConfig(YamlConfiguration config) {
        this.config = config;
    }

    public void load() throws IOException {
        this.config.load();
        this.signatureSecret = config.getString("signature.secret");
        this.maxClipboardSize = config.getLong("clipboard.max-size");
        this.enableClipboardAutoUpload = config.getBoolean("clipboard.enable-auto-upload");
        this.enableClipboardAutoDownload = config.getBoolean("clipboard.enable-auto-download");
        this.enableKeepClipboard = config.getBoolean("clipboard.keep.enable");
        this.enableClipboardAutoCleanup = config.getBoolean("clipboard.keep.enable-auto-cleanup");
    }

    public String getSignatureSecret() {
        return signatureSecret;
    }

    @Override
    public long getMaxClipboardSize() {
        return maxClipboardSize;
    }

    @Override
    public boolean isClipboardAutoUploadEnabled() {
        return enableClipboardAutoUpload;
    }

    public boolean isClipboardAutoDownloadEnabled() {
        return enableClipboardAutoDownload;
    }

    public boolean isKeepClipboardEnabled() {
        return enableKeepClipboard;
    }

    public boolean isClipboardAutoCleanupEnabled() {
        return enableClipboardAutoCleanup;
    }
}
