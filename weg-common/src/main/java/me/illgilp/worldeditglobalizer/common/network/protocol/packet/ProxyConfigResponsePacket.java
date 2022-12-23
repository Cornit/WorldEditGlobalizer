package me.illgilp.worldeditglobalizer.common.network.protocol.packet;

import java.io.IOException;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.illgilp.worldeditglobalizer.common.config.CommonProxyConfig;
import me.illgilp.worldeditglobalizer.common.network.AbstractPacketHandler;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataInput;
import me.illgilp.worldeditglobalizer.common.network.data.PacketDataOutput;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProxyConfigResponsePacket extends IdentifiedPacket {

    private CommonProxyConfig config;

    public ProxyConfigResponsePacket(UUID id, boolean request, CommonProxyConfig config) {
        super(id, request);
        this.config = config;
    }

    public ProxyConfigResponsePacket(CommonProxyConfig config) {
        this.config = config;
    }

    @Override
    public void readData(PacketDataInput in) throws IOException {
        this.config = new CommonProxyConfigImpl(
            in.readVarLong(),
            in.readBoolean()
        );
    }

    @Override
    public void writeData(PacketDataOutput out) throws IOException {
        out.writeVarLong(this.config.getMaxClipboardSize());
        out.writeBoolean(this.config.isClipboardAutoUploadEnabled());
    }

    @Override
    public void handle(AbstractPacketHandler packetHandler) {
        packetHandler.handle(this);
    }

    private static final class CommonProxyConfigImpl extends CommonProxyConfig {

        private final long maxClipboardSize;
        private final boolean clipboardAutoUploadEnabled;

        public CommonProxyConfigImpl(long maxClipboardSize, boolean clipboardAutoUploadEnabled) {
            this.maxClipboardSize = maxClipboardSize;
            this.clipboardAutoUploadEnabled = clipboardAutoUploadEnabled;
        }

        @Override
        public long getMaxClipboardSize() {
            return maxClipboardSize;
        }

        @Override
        public boolean isClipboardAutoUploadEnabled() {
            return clipboardAutoUploadEnabled;
        }
    }

}
