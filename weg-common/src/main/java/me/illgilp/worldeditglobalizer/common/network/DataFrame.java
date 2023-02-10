package me.illgilp.worldeditglobalizer.common.network;

import java.io.IOException;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.illgilp.worldeditglobalizer.common.WegVersion;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataInput;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataOutput;

@Data
@NoArgsConstructor
@AllArgsConstructor
class DataFrame {

    private static final int MAX_FRAME_SIZE = 32766;
    public static final int MAX_FRAME_PAYLOAD_SIZE =
        MAX_FRAME_SIZE
            - WegVersion.BYTES_SIZE
            - 16 // UUID frameId
            - 1 //  boolean hasMore
            - 1 // boolean cancelled
            - 32 // byte[] signature
        ;

    private UUID frameId;
    private boolean moreFollowing;
    private boolean cancelled;
    private byte[] payload;


    void read(PacketDataInput in) throws IOException {
        this.frameId = in.readUUID();
        this.moreFollowing = in.readBoolean();
        this.cancelled = in.readBoolean();
        this.payload = in.readRemainingBytes();
    }

    void write(PacketDataOutput out) throws IOException {
        out.writeUUID(this.frameId);
        out.writeBoolean(this.moreFollowing);
        out.writeBoolean(this.cancelled);
        out.writeRawBytes(this.payload);
    }
}
