package me.illgilp.worldeditglobalizercommon.network.packets;

import java.util.UUID;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;

public class PluginConfigResponsePacket extends Packet {

    private UUID identifier = UUID.randomUUID();
    private String language;
    private long maxClipboardSize;
    private boolean keepClipboard;
    private String prefix;
    private boolean enableClipboardAutoDownload;
    private boolean enableClipboardAutoUpload;

    public PluginConfigResponsePacket() {
    }

    @Override
    public void read(PacketDataSerializer buf) {
        identifier = UUID.fromString(buf.readString());
        language = buf.readString();
        maxClipboardSize = buf.readLong();
        keepClipboard = buf.readBoolean();
        prefix = buf.readString();
        enableClipboardAutoDownload = buf.readBoolean();
        enableClipboardAutoUpload = buf.readBoolean();
    }

    @Override
    public void write(PacketDataSerializer buf) {
        buf.writeString(identifier.toString());
        buf.writeString(language);
        buf.writeLong(maxClipboardSize);
        buf.writeBoolean(keepClipboard);
        buf.writeString(prefix);
        buf.writeBoolean(enableClipboardAutoDownload);
        buf.writeBoolean(enableClipboardAutoUpload);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PluginConfigResponsePacket that = (PluginConfigResponsePacket) o;

        if (maxClipboardSize != that.maxClipboardSize) {
            return false;
        }
        if (keepClipboard != that.keepClipboard) {
            return false;
        }
        if (enableClipboardAutoDownload != that.enableClipboardAutoDownload) {
            return false;
        }
        if (enableClipboardAutoUpload != that.enableClipboardAutoUpload) {
            return false;
        }
        if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) {
            return false;
        }
        if (language != null ? !language.equals(that.language) : that.language != null) {
            return false;
        }
        return prefix != null ? prefix.equals(that.prefix) : that.prefix == null;
    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (int) (maxClipboardSize ^ (maxClipboardSize >>> 32));
        result = 31 * result + (keepClipboard ? 1 : 0);
        result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
        result = 31 * result + (enableClipboardAutoDownload ? 1 : 0);
        result = 31 * result + (enableClipboardAutoUpload ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PluginConfigResponsePacket{" +
            "identifier=" + identifier +
            ", language='" + language + '\'' +
            ", maxClipboardSize=" + maxClipboardSize +
            ", keepClipboard=" + keepClipboard +
            ", prefix='" + prefix + '\'' +
            ", enableClipboardAutoDownload=" + enableClipboardAutoDownload +
            ", enableClipboardAutoUpload=" + enableClipboardAutoUpload +
            "} ";
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public void setIdentifier(UUID identifier) {
        this.identifier = identifier;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public long getMaxClipboardSize() {
        return maxClipboardSize;
    }

    public void setMaxClipboardSize(long maxClipboardSize) {
        this.maxClipboardSize = maxClipboardSize;
    }

    public boolean isKeepClipboard() {
        return keepClipboard;
    }

    public void setKeepClipboard(boolean keepClipboard) {
        this.keepClipboard = keepClipboard;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isEnableClipboardAutoDownload() {
        return enableClipboardAutoDownload;
    }

    public void setEnableClipboardAutoDownload(boolean enableClipboardAutoDownload) {
        this.enableClipboardAutoDownload = enableClipboardAutoDownload;
    }

    public boolean isEnableClipboardAutoUpload() {
        return enableClipboardAutoUpload;
    }

    public void setEnableClipboardAutoUpload(boolean enableClipboardAutoUpload) {
        this.enableClipboardAutoUpload = enableClipboardAutoUpload;
    }
}
