package me.illgilp.worldeditglobalizerbukkit.network.packets;

import me.illgilp.worldeditglobalizerbukkit.util.PacketDataSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PermissionCheckResponsePacket extends Packet {

    private UUID identifier = UUID.randomUUID();
    private UUID player;
    private Map<String,Boolean> permissions;



    @Override
    public void read(PacketDataSerializer buf) {
        identifier = UUID.fromString(buf.readString());
        player = UUID.fromString(buf.readString());
        int length = buf.readVarInt();
        permissions = new HashMap<>();
        for(int i = 0; i<length;i++){
            String key = buf.readString();
            Boolean granted = buf.readBoolean();
            permissions.put(key,granted);
        }
    }

    @Override
    public void write(PacketDataSerializer buf) {
        buf.writeString(identifier.toString());
        buf.writeString(player.toString());
        buf.writeVarInt(permissions.size());
        for(String key : permissions.keySet()){
            buf.writeString(key);
            buf.writeBoolean(permissions.get(key));
        }
    }

    public UUID getPlayer() {
        return player;
    }

    public void setPlayer(UUID player) {
        this.player = player;
    }

    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, Boolean> permissions) {
        this.permissions = permissions;
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public void setIdentifier(UUID identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PermissionCheckResponsePacket)) return false;

        PermissionCheckResponsePacket that = (PermissionCheckResponsePacket) o;

        if (!getIdentifier().equals(that.getIdentifier())) return false;
        if (!getPlayer().equals(that.getPlayer())) return false;
        return getPermissions().equals(that.getPermissions());
    }

    @Override
    public int hashCode() {
        int result = getIdentifier().hashCode();
        result = 31 * result + getPlayer().hashCode();
        result = 31 * result + getPermissions().hashCode();

        return result;
    }

    @Override
    public String toString() {
        return "PermissionCheckResponsePacket{" +
                "identifier=" + identifier +
                ", player=" + player +
                ", permissions=" + permissions +
                '}';
    }
}
