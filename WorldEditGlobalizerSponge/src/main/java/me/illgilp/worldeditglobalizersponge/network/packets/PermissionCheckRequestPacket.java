package me.illgilp.worldeditglobalizersponge.network.packets;

import me.illgilp.worldeditglobalizersponge.util.PacketDataSerializer;

import java.util.Arrays;
import java.util.UUID;

public class PermissionCheckRequestPacket extends Packet {

    private UUID identifier = UUID.randomUUID();
    private UUID player;
    private String[] permissions;



    @Override
    public void read(PacketDataSerializer buf) {
        identifier = UUID.fromString(buf.readString());
        player = UUID.fromString(buf.readString());
        int length = buf.readVarInt();
        permissions = new String[length];
        for(int i = 0; i<length;i++){
            permissions[i] = buf.readString();
        }
    }

    @Override
    public void write(PacketDataSerializer buf) {
        buf.writeString(identifier.toString());
        buf.writeString(player.toString());
        buf.writeVarInt(permissions.length);
        for(int i = 0;i<permissions.length;i++){
            buf.writeString(permissions[i]);
        }
    }

    public UUID getPlayer() {
        return player;
    }

    public void setPlayer(UUID player) {
        this.player = player;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public void setPermissions(String[] permissions) {
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
        if (!(o instanceof PermissionCheckRequestPacket)) return false;

        PermissionCheckRequestPacket that = (PermissionCheckRequestPacket) o;

        if (!getPlayer().equals(that.getPlayer())) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(getPermissions(), that.getPermissions());
    }

    @Override
    public int hashCode() {
        int result = getPlayer().hashCode();
        result = 31 * result + Arrays.hashCode(getPermissions());
        return result;
    }

    @Override
    public String toString() {
        return "PermissionCheckRequestPacket{" +
                "player=" + player +
                ", permissions=" + Arrays.toString(permissions) +
                '}';
    }
}
