package me.illgilp.worldeditglobalizercommon.network.packets;


import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;

import java.util.Arrays;
import java.util.UUID;

public class MessageRequestPacket extends Packet {

    private UUID identifier = UUID.randomUUID();
    private String path;
    private String language = "default";
    private Object[] placeholders = new Object[0];


    @Override
    public void read(PacketDataSerializer buf) {
        identifier = UUID.fromString(buf.readString());
        path = buf.readString();
        language = buf.readString();
        placeholders = new Object[buf.readVarInt()];
        for (int i = 0; i < placeholders.length; i++) {
            placeholders[i] = buf.readString();
        }
    }

    @Override
    public void write(PacketDataSerializer buf) {
        buf.writeString(identifier.toString());
        buf.writeString(path);
        buf.writeString(language);
        buf.writeVarInt(placeholders.length);
        for (int i = 0; i < placeholders.length; i++) {
            buf.writeString(placeholders[i].toString());
        }
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public void setIdentifier(UUID identifier) {
        this.identifier = identifier;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Object[] getPlaceholders() {
        return placeholders;
    }

    public void setPlaceholders(Object[] placeholders) {
        this.placeholders = placeholders;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageRequestPacket)) return false;

        MessageRequestPacket that = (MessageRequestPacket) o;

        if (!getIdentifier().equals(that.getIdentifier())) return false;
        if (!getPath().equals(that.getPath())) return false;
        if (!getLanguage().equals(that.getLanguage())) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(getPlaceholders(), that.getPlaceholders());
    }

    @Override
    public int hashCode() {
        int result = getIdentifier().hashCode();
        result = 31 * result + getPath().hashCode();
        result = 31 * result + getLanguage().hashCode();
        result = 31 * result + Arrays.hashCode(getPlaceholders());
        return result;
    }

    @Override
    public String toString() {
        return "MessageRequestPacket{" +
                "identifier=" + identifier +
                ", path='" + path + '\'' +
                ", language='" + language + '\'' +
                ", placeholders=" + Arrays.toString(placeholders) +
                '}';
    }
}
