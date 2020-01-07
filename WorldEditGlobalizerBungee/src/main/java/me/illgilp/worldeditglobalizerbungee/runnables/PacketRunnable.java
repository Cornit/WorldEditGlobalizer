package me.illgilp.worldeditglobalizerbungee.runnables;

import me.illgilp.worldeditglobalizercommon.network.packets.Packet;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public abstract class PacketRunnable implements Runnable {


    private ProxiedPlayer player;
    private Packet packet;

    public PacketRunnable(ProxiedPlayer player, Packet packet) {
        this.player = player;
        this.packet = packet;
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }

    public Packet getPacket() {
        return packet;
    }
}
