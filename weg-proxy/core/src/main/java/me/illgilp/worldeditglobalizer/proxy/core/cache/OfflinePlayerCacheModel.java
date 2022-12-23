package me.illgilp.worldeditglobalizer.proxy.core.cache;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataInput;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataOutput;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OfflinePlayerCacheModel {

    private UUID uuid;
    private String name;
    private Instant expiresAt;

    public void write(PacketDataOutput out) throws IOException {
        out.writeUUID(uuid);
        out.writeString(name);
        out.writeLong(expiresAt.toEpochMilli());
    }

    public void read(PacketDataInput in) throws IOException {
        this.uuid = in.readUUID();
        this.name = in.readString();
        this.expiresAt = Instant.ofEpochMilli(in.readLong());
    }

    @Override
    public String toString() {
        return "OfflinePlayerCacheModel{" +
            "uuid=" + uuid +
            ", name='" + name + '\'' +
            ", expiresAt=" + expiresAt +
            '}';
    }
}
