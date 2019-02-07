package me.illgilp.worldeditglobalizerbukkit.events;


import me.illgilp.worldeditglobalizercommon.network.packets.Packet;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PacketReceivedEvent extends Event {

    private static HandlerList haandlers = new HandlerList();

    private Player player;
    private Packet packet;
    private int packetid;

    public PacketReceivedEvent(Player player, Packet packet, int packetid) {
        this.player = player;
        this.packet = packet;
        this.packetid = packetid;
    }


    public Player getPlayer() {
        return player;
    }

    public Packet getPacket() {
        return packet;
    }

    public int getPacketId() {
        return packetid;
    }

    public static HandlerList getHandlerList() {
        return haandlers;
    }

    @Override
    public HandlerList getHandlers() {
        return haandlers;
    }
}
