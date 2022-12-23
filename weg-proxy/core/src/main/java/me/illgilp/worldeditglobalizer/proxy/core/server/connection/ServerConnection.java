package me.illgilp.worldeditglobalizer.proxy.core.server.connection;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import me.illgilp.worldeditglobalizer.common.ProgressListener;
import me.illgilp.worldeditglobalizer.common.network.AbstractPacketHandler;
import me.illgilp.worldeditglobalizer.common.network.NetworkManager;
import me.illgilp.worldeditglobalizer.common.network.exception.IncompatibleVersionException;
import me.illgilp.worldeditglobalizer.common.network.exception.InvalidSignatureException;
import me.illgilp.worldeditglobalizer.common.network.exception.PacketHandleException;
import me.illgilp.worldeditglobalizer.common.network.protocol.PacketFactory;
import me.illgilp.worldeditglobalizer.common.network.protocol.Protocol;
import me.illgilp.worldeditglobalizer.common.network.protocol.ProtocolPacketFactory;
import me.illgilp.worldeditglobalizer.common.network.protocol.packet.Packet;
import me.illgilp.worldeditglobalizer.common.permission.Permission;
import me.illgilp.worldeditglobalizer.common.scheduler.WegScheduler;
import me.illgilp.worldeditglobalizer.proxy.core.api.WegProxy;
import me.illgilp.worldeditglobalizer.proxy.core.api.server.WegServer;
import me.illgilp.worldeditglobalizer.proxy.core.api.server.WegServerInfo;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ServerConnection extends NetworkManager implements WegServer {

    public static final Key PLUGIN_MESSAGE_CHANNEL = Key.key("weg", "connection");

    @Getter
    private State state = State.UNKNOWN;

    @Getter
    private final WegServerInfo serverInfo;

    private final ServerConnectionListener serverConnectionListener;

    private final Map<Permission, TriState> cachedPermissions = new HashMap<>();

    public ServerConnection(ServerConnectionListener serverConnectionListener, AbstractPacketHandler packetHandler, WegServerInfo serverInfo) {
        super(packetHandler);
        this.serverConnectionListener = serverConnectionListener;
        this.serverInfo = serverInfo;
        for (Permission value : Permission.values()) {
            cachedPermissions.put(value, TriState.NOT_SET);
        }
    }

    @Override
    protected PacketFactory getIncomingPacketFactory() {
        return new ProtocolPacketFactory(Protocol.TO_PROXY);
    }

    @Override
    protected PacketFactory getOutgoingPacketFactory() {
        return new ProtocolPacketFactory(Protocol.TO_SERVER);
    }

    @Override
    protected byte[] getSigningSecret() {
        return WegProxy.getInstance().getProxyConfig().getSignatureSecret().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void handleBytes(byte[] data) {
        try {
            super.handleBytes(data);
        } catch (InvalidSignatureException e) {
            setState(State.WRONG_CONFIGURATION);
            return;
        } catch (IncompatibleVersionException e) {
            setState(State.INCOMPATIBLE);
            return;
        } catch (Throwable e) {
            throw new PacketHandleException(e);
        }
        setState(State.USABLE);
    }

    @Override
    public boolean isUsable() {
        return this.state == State.USABLE;
    }

    private void setState(State state) {
        if (this.state != state) {
            State old = this.state;
            this.state = state;
            this.serverConnectionListener.handleStateChange(old, state);
        }
    }

    @Override
    public void sendPacket(@NotNull Packet packet, @Nullable ProgressListener progressListener) {
        WegScheduler.getInstance().getAsyncPacketWriteExecutor().execute(() -> super.sendPacket(packet, progressListener));
    }

    @Override
    protected void scheduleFrameSend(Runnable runnable, int index) {
        WegScheduler.getInstance().getAsyncPacketWriteExecutor().schedule(runnable, 100L * index, TimeUnit.MILLISECONDS);
    }

    public TriState getCachedPermissionValue(Permission permission) {
        return cachedPermissions.getOrDefault(permission, TriState.NOT_SET);
    }

    public TriState setPermissionValue(Permission key, TriState value) {
        return cachedPermissions.put(key, value);
    }
}
