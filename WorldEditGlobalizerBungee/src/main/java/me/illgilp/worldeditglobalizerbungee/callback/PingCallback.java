package me.illgilp.worldeditglobalizerbungee.callback;

import java.util.UUID;
import me.illgilp.worldeditglobalizerbungee.player.Player;
import me.illgilp.worldeditglobalizercommon.network.PacketDataSerializer;

public abstract class PingCallback<U> extends Callback<U,Boolean> {

    private Player player;

    public PingCallback(long timeOut, Player player) {
        super(timeOut, UUID.randomUUID());
        this.player = player;
    }


    @Override
    public void start() {
        PacketDataSerializer packetDataSerializer = new PacketDataSerializer();
        packetDataSerializer.writeUUID(this.getIdentifier());
        player.getProxiedPlayer().getServer().sendData("weg:ping", packetDataSerializer.toByteArray());
        super.start();
    }
}
