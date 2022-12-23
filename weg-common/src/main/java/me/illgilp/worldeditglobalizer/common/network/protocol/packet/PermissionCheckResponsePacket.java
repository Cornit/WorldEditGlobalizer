package me.illgilp.worldeditglobalizer.common.network.protocol.packet;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.illgilp.worldeditglobalizer.common.network.AbstractPacketHandler;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataInput;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataOutput;
import me.illgilp.worldeditglobalizer.common.permission.Permission;
import net.kyori.adventure.util.TriState;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PermissionCheckResponsePacket extends IdentifiedPacket {

    private Map<Permission, TriState> permissions = new HashMap<>();

    public PermissionCheckResponsePacket(UUID id, boolean request, Map<Permission, TriState> permissions) {
        super(id, request);
        this.permissions = permissions;
    }

    public PermissionCheckResponsePacket(Map<Permission, TriState> permissions) {
        this.permissions = permissions;
    }

    @Override
    public void readData(PacketDataInput in) throws IOException {
        permissions = new HashMap<>();
        int size = in.readVarInt();
        for (int i = 0; i < size; i++) {
            String key = in.readString();
            TriState state;
            switch (in.readByte()) {
                case 1:
                    state = TriState.TRUE;
                    break;
                case 2:
                    state = TriState.FALSE;
                    break;
                default:
                    state = TriState.NOT_SET;
                    break;
            }
            permissions.put(Arrays.stream(Permission.values())
                .filter(p -> p.getPermission().equals(key))
                .findFirst()
                .orElse(null), state);
        }
    }

    @Override
    public void writeData(PacketDataOutput out) throws IOException {
        out.writeVarInt(this.permissions.size());
        for (Map.Entry<Permission, TriState> entry : this.permissions.entrySet()) {
            out.writeString(entry.getKey().getPermission());
            byte state = 0;
            switch (entry.getValue()) {
                case FALSE:
                    state = 2;
                    break;
                case TRUE:
                    state = 1;
                    break;
                default:
                    break;
            }
            out.writeByte(state);
        }
    }

    @Override
    public void handle(AbstractPacketHandler packetHandler) {
        packetHandler.handle(this);
    }
}
