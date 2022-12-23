package me.illgilp.worldeditglobalizer.common.network.protocol.packet;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.illgilp.worldeditglobalizer.common.network.AbstractPacketHandler;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataInput;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataOutput;
import me.illgilp.worldeditglobalizer.common.permission.Permission;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PermissionCheckRequestPacket extends IdentifiedPacket {

    private Permission[] permissions;

    public PermissionCheckRequestPacket(UUID id, boolean request, Permission... permissions) {
        super(id, request);
        this.permissions = permissions;
    }

    public PermissionCheckRequestPacket(Permission... permissions) {
        this.permissions = permissions;
    }

    @Override
    public void readData(PacketDataInput in) throws IOException {
        this.permissions = new Permission[in.readVarInt()];
        for (int i = 0; i < this.permissions.length; i++) {
            final String perm = in.readString();
            this.permissions[i] = Arrays.stream(Permission.values())
                .filter(p -> p.getPermission().equals(perm))
                .findFirst()
                .orElse(null);
        }
    }

    @Override
    public void writeData(PacketDataOutput out) throws IOException {
        out.writeVarInt(this.permissions.length);
        for (Permission permission : this.permissions) {
            out.writeString(permission.getPermission());
        }
    }

    @Override
    public void handle(AbstractPacketHandler packetHandler) {
        packetHandler.handle(this);
    }
}
