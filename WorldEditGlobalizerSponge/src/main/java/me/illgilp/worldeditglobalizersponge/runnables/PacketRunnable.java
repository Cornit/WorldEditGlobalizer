package me.illgilp.worldeditglobalizersponge.runnables;

import me.illgilp.worldeditglobalizersponge.network.packets.Packet;
import me.illgilp.worldeditglobalizersponge.task.QueuedAsyncTask;
import org.spongepowered.api.entity.living.player.Player;

public abstract class PacketRunnable extends QueuedAsyncTask {


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
