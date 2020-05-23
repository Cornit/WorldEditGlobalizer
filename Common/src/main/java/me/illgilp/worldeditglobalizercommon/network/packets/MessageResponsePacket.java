package me.illgilp.worldeditglobalizercommon.network.packets;


import java.util.UUID;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;

public class MessageResponsePacket extends Packet {

    private UUID identifier = UUID.randomUUID();
    private String path;
    private String language = "default";
    private String json = "none";


    @Override
    public void read(PacketDataSerializer buf) {
        identifier = UUID.fromString(buf.readString());
        path = buf.readString();
        language = buf.readString();
        json = buf.readString();
    }

    @Override
    public void write(PacketDataSerializer buf) {
        buf.writeString(identifier.toString());
        buf.writeString(path);
        buf.writeString(language);
        buf.writeString(json);
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

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageResponsePacket)) return false;

        MessageResponsePacket that = (MessageResponsePacket) o;

        if (!getIdentifier().equals(that.getIdentifier())) return false;
        if (!getPath().equals(that.getPath())) return false;
        if (!getLanguage().equals(that.getLanguage())) return false;
        return getJson().equals(that.getJson());
    }

    @Override
    public int hashCode() {
        int result = getIdentifier().hashCode();
        result = 31 * result + getPath().hashCode();
        result = 31 * result + getLanguage().hashCode();
        result = 31 * result + getJson().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MessageResponsePacket{" +
                "identifier=" + identifier +
                ", path='" + path + '\'' +
                ", language='" + language + '\'' +
                ", json='" + json + '\'' +
                '}';
    }
}
