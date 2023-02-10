package me.illgilp.worldeditglobalizer.server.core.server.connection;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.Getter;
import me.illgilp.worldeditglobalizer.common.network.AbstractPacketHandler;
import me.illgilp.worldeditglobalizer.common.network.NetworkManager;
import me.illgilp.worldeditglobalizer.common.network.PacketSender;
import me.illgilp.worldeditglobalizer.common.network.exception.IncompatibleVersionException;
import me.illgilp.worldeditglobalizer.common.network.exception.InvalidSignatureException;
import me.illgilp.worldeditglobalizer.common.network.exception.PacketHandleException;
import me.illgilp.worldeditglobalizer.common.network.protocol.PacketFactory;
import me.illgilp.worldeditglobalizer.common.network.protocol.Protocol;
import me.illgilp.worldeditglobalizer.common.network.protocol.ProtocolPacketFactory;
import me.illgilp.worldeditglobalizer.server.core.api.WegServer;
import me.illgilp.worldeditglobalizer.server.core.server.WegServerConnection;
import net.kyori.adventure.key.Key;

public abstract class ServerConnection extends NetworkManager implements WegServerConnection {

    public static final Key PLUGIN_MESSAGE_CHANNEL = Key.key("weg", "connection");

    @Getter
    private State state = State.UNKNOWN;

    public ServerConnection(AbstractPacketHandler packetHandler, PacketSender packetSender) {
        super(packetHandler, packetSender);
    }

    @Override
    protected PacketFactory getIncomingPacketFactory() {
        return new ProtocolPacketFactory(Protocol.TO_SERVER);
    }

    @Override
    protected PacketFactory getOutgoingPacketFactory() {
        return new ProtocolPacketFactory(Protocol.TO_PROXY);
    }

    @Override
    protected byte[] getSigningSecret() {
        String secret = WegServer.getInstance().getServerConfig().getSignatureSecret();
        if (secret.equals("PUT SIGNATURE KEY HERE")) {
            secret = (UUID.randomUUID() + UUID.randomUUID().toString())
                .replace("-", "");
        }
        return secret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void handleBytes(byte[] data) {
        try {
            super.handleBytes(data);
        } catch (InvalidSignatureException e) {
            this.state = State.WRONG_CONFIGURATION;
            return;
        } catch (IncompatibleVersionException e) {
            this.state = State.INCOMPATIBLE;
            return;
        } catch (Throwable e) {
            throw new PacketHandleException(e);
        }
        this.state = State.USABLE;
    }
}
