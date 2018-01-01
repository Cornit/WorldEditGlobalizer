package me.illgilp.worldeditglobalizerbukkit.network.packets;


import me.illgilp.worldeditglobalizerbukkit.util.PacketDataSerializer;

import java.util.UUID;

public class PluginConfigResponsePacket extends Packet {

    private UUID identifier = UUID.randomUUID();
    private String language;
    private long maxClipboardSize;
    private boolean keepClipboard;
    private String prefix;

    @Override
    public void read(PacketDataSerializer buf) {
        identifier = UUID.fromString(buf.readString());
        language = buf.readString();
        maxClipboardSize = buf.readLong();
        keepClipboard = buf.readBoolean();
        prefix = buf.readString();
    }

    @Override
    public void write(PacketDataSerializer buf) {
        buf.writeString(identifier.toString());
        buf.writeString(language);
        buf.writeLong(maxClipboardSize);
        buf.writeBoolean(keepClipboard);
        buf.writeString(prefix);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PluginConfigResponsePacket)) return false;

        PluginConfigResponsePacket that = (PluginConfigResponsePacket) o;

        if (getMaxClipboardSize() != that.getMaxClipboardSize()) return false;
        if (isKeepClipboard() != that.isKeepClipboard()) return false;
        if (!identifier.equals(that.identifier)) return false;
        if (!getLanguage().equals(that.getLanguage())) return false;
        return getPrefix().equals(that.getPrefix());
    }

    @Override
    public int hashCode() {
        int result = identifier.hashCode();
        result = 31 * result + getLanguage().hashCode();
        result = 31 * result + (int) (getMaxClipboardSize() ^ (getMaxClipboardSize() >>> 32));
        result = 31 * result + (isKeepClipboard() ? 1 : 0);
        result = 31 * result + getPrefix().hashCode();
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
                '}';
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
}
