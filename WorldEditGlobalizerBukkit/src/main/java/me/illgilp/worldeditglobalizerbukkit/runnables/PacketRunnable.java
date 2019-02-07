package me.illgilp.worldeditglobalizerbukkit.runnables;

import me.illgilp.worldeditglobalizercommon.network.packets.Packet;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class PacketRunnable extends BukkitRunnable {


    private Player player;
    private Packet packet;

    public PacketRunnable(Player player, Packet packet) {
        this.player = player;
        this.packet = packet;
    }

    public Player getPlayer() {
        return player;
    }

    public Packet getPacket() {
        return packet;
    }
}
